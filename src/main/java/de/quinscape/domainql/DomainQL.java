package de.quinscape.domainql;

import de.quinscape.domainql.annotation.GraphQLInput;
import de.quinscape.domainql.config.Options;
import de.quinscape.domainql.config.RelationConfiguration;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.fetcher.BackReferenceFetcher;
import de.quinscape.domainql.fetcher.SvensonFetcher;
import de.quinscape.domainql.logic.DomainQLMethod;
import de.quinscape.domainql.logic.GraphQLValueProvider;
import de.quinscape.domainql.logic.LogicBeanAnalyzer;
import de.quinscape.domainql.logic.Mutation;
import de.quinscape.domainql.logic.Query;
import de.quinscape.domainql.param.ParameterProvider;
import de.quinscape.domainql.param.ParameterProviderFactory;
import de.quinscape.domainql.scalar.GraphQLDateScalar;
import de.quinscape.domainql.scalar.GraphQLTimestampScalar;
import de.quinscape.domainql.util.JSONUtil;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import org.apache.commons.beanutils.ConvertUtils;
import org.jooq.DSLContext;
import org.jooq.ForeignKey;
import org.jooq.Table;
import org.jooq.TableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.beans.Introspector;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static graphql.schema.GraphQLNonNull.nonNull;

/**
 * Annotation-based convention-over-configuration GraphQL Schema helper.
 */
public class DomainQL
{
    private final static Logger log = LoggerFactory.getLogger(DomainQL.class);

    private final static Map<Class<?>, GraphQLScalarType> JAVA_TYPE_TO_GRAPHQL;


    static
    {
        final HashMap<Class<?>, GraphQLScalarType> map = new HashMap<>();

        map.put(Boolean.class, Scalars.GraphQLBoolean);
        map.put(Boolean.TYPE, Scalars.GraphQLBoolean);
        map.put(Byte.class, Scalars.GraphQLByte);
        map.put(Byte.TYPE, Scalars.GraphQLByte);
        map.put(Short.class, Scalars.GraphQLShort);
        map.put(Short.TYPE, Scalars.GraphQLShort);
        map.put(Integer.class, Scalars.GraphQLInt);
        map.put(Integer.TYPE, Scalars.GraphQLInt);
        map.put(Long.class, Scalars.GraphQLLong);
        map.put(Long.TYPE, Scalars.GraphQLLong);
        map.put(String.class, Scalars.GraphQLString);
        map.put(BigDecimal.class, Scalars.GraphQLBigDecimal);
        map.put(BigInteger.class, Scalars.GraphQLBigInteger);
        map.put(Timestamp.class, new GraphQLTimestampScalar());
        map.put(Date.class, new GraphQLDateScalar());

        JAVA_TYPE_TO_GRAPHQL = Collections.unmodifiableMap(map);
    }

    private final Collection<ParameterProviderFactory> parameterProviderFactories;

    private final Options options;

    private final Map<Class<?>, GraphQLOutputType> registeredOutputTypes;
    private final Map<Class<?>, GraphQLInputType> registeredInputTypes;

    private final Set<Query> queries = new LinkedHashSet<>();

    private final Set<Mutation> mutations = new LinkedHashSet<>();

    private final DSLContext dslContext;

    private final Set<Object> logicBeans;

    private final Set<Table<?>> jooqTables;

    private final boolean mirrorInputs;

    private final Set<Class<?>> inputTypes;

    private final Map<ForeignKey<?, ?>, RelationConfiguration> relationConfigurations;

    private final RelationConfiguration defaultRelationConfiguration;


    DomainQL(
        DSLContext dslContext,
        Set<Object> logicBeans,
        Set<Table<?>> jooqTables,
        Set<Class<?>> inputTypes,
        Collection<ParameterProviderFactory> parameterProviderFactories,
        Map<ForeignKey<?, ?>, RelationConfiguration> relationConfigurations,
        RelationConfiguration defaultRelationConfiguration,
        Options options,
        boolean mirrorInputs
    )
    {
        this.dslContext = dslContext;
        this.logicBeans = logicBeans;
        this.jooqTables = jooqTables;
        this.mirrorInputs = mirrorInputs;
        this.inputTypes = inputTypes;
        this.relationConfigurations = relationConfigurations;
        this.defaultRelationConfiguration = defaultRelationConfiguration;

        registeredOutputTypes = new HashMap<>(JAVA_TYPE_TO_GRAPHQL);
        registeredInputTypes = new HashMap<>(JAVA_TYPE_TO_GRAPHQL);

        this.parameterProviderFactories = parameterProviderFactories;
        this.options = options;
    }


    /**
     * Returns the GraphQL type associated with the given java type.
     * <p>
     * These are the types common to all DomainQL definitions. The supported
     * types are
     *
     * <ul>
     * <li>Primitives and their object wrappers</li>
     * <li>String</li>
     * <li>BigDecimal</li>
     * <li>BigInteger</li>
     * <li>java.sql.Timestamp</li>
     * <li>java.sql.Date</li>
     * </ul>
     *
     * @param cls java type
     * @return GraphQL scalar type
     */
    public static GraphQLScalarType getGraphQLScalarFor(Class<?> cls)
    {
        return JAVA_TYPE_TO_GRAPHQL.get(cls);
    }


    public GraphQLOutputType getOutputType(Class<?> cls)
    {
        return registeredOutputTypes.get(cls);
    }

    public GraphQLInputType getInputType(Class<?> cls)
    {
        return registeredInputTypes.get(cls);
    }



    private Class<?> findPojoTypeOf(Table<?> table) throws ClassNotFoundException
    {
        final String typeName = table.getClass().getSimpleName();
        return Class.forName(
            // following jooq code generator conventions
            table.getClass().getPackage().getName() + ".pojos." + typeName
        );
    }


    private String findJsonName(JSONClassInfo classInfo, String javaName)
    {
        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {
            if (info.getJavaPropertyName().equals(javaName))
            {
                return info.getJsonName();
            }
        }
        throw new IllegalStateException("Could not find JSON property info for java name " + javaName);
    }


    private Set<String> findForeignKeyFields(Table<?> table)
    {
        Set<String> set = new HashSet<>();
        for (ForeignKey<?, ?> foreignKey : table.getReferences())
        {
            for (TableField<?, ?> field : foreignKey.getFields())
            {
                set.add(field.getName());
            }
        }
        return set;
    }


    private Set<ForeignKey<?, ?>> findBackReferences(Table<?> table, Set<Table<?>> tables)
    {
        Set<ForeignKey<?, ?>> set = new LinkedHashSet<>();
        for (Table<?> other : tables)
        {
            for (ForeignKey<?, ?> foreignKey : other.getReferences())
            {
                if (foreignKey.getFields().size() == 1 &&
                    foreignKey.getKey().getTable().equals(table) &&
                    getRelationConfiguration(foreignKey).getTargetField() != TargetField.NONE)
                {
                    set.add(foreignKey);
                }
            }
        }
        return set;
    }


    private Column getColumnAnnotation(Class<?> pojoType, JSONPropertyInfo info)
    {
        final Column jpaColumnAnno = JSONUtil.findAnnotation(info, Column.class);
        if (jpaColumnAnno == null)
        {
            throw new IllegalStateException(
                "No @Column annotation on property " + pojoType.getSimpleName() + "." + info.getJavaPropertyName());
        }
        return jpaColumnAnno;
    }


    private JSONPropertyInfo findPropertyInfoForField(Table<?> table, Class<?> pojoType, TableField<?, ?> tableField)
    {
        final JSONClassInfo classInfo = JSONUtil.getClassInfo(pojoType);

        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {
            final Class<Object> type = info.getType();
            final Column jpaColumnAnno = getColumnAnnotation(pojoType, info);

            final String fieldName = tableField.getName();
            if (fieldName.equals(jpaColumnAnno.name()))
            {
                return info;
            }
        }
        throw new DomainQLException("Cannot find property info for field " + tableField.getName() + " in " + pojoType.getName());
    }


    private void registerLogic(
        GraphQLSchema.Builder builder, Collection<Object> logicBeans, Map<Class<?>, String> inputTypes
    )
    {
        log.debug("registerLogic {}", logicBeans);

        LogicBeanAnalyzer analyzer = new LogicBeanAnalyzer(this, parameterProviderFactories, logicBeans, inputTypes);

        final Set<Query> queries = analyzer.getQueries();
        final Set<Mutation> mutations = analyzer.getMutations();

        final GraphQLObjectType.Builder queryTypeBuilder = GraphQLObjectType.newObject()
            .name("DomainQueries")
            .description("Auto-generated queries by the DomainQLHelper");

        final GraphQLObjectType.Builder mutationTypeBuilder = GraphQLObjectType.newObject()
            .name("DomainMutations")
            .description("Auto-generated mutations by the DomainQLHelper");

        this.queries.addAll(queries);
        this.mutations.addAll(mutations);

        for (Query query : queries)
        {
            List<GraphQLArgument> arguments = getGraphQLArguments(query);

            queryTypeBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
                .name(query.getName())
                .type(query.getResultType())
                .dataFetcher(query)
                .argument(arguments)
                .description(query.getDescription())
                .build());

        }

        for (Mutation mutation : mutations)
        {
            List<GraphQLArgument> arguments = getGraphQLArguments(mutation);

            mutationTypeBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
                .name(mutation.getName())
                .type(mutation.getResultType())
                .dataFetcher(mutation)
                .argument(arguments)
                .description(mutation.getDescription())
                .build());

        }

        builder.query(queryTypeBuilder);
        builder.mutation(mutationTypeBuilder);
    }


    private List<GraphQLArgument> getGraphQLArguments(DomainQLMethod query)
    {
        List<GraphQLArgument> arguments = new ArrayList<>();
        for (ParameterProvider provider : query.getParameterProviders())
        {
            if (provider instanceof GraphQLValueProvider)
            {
                final GraphQLValueProvider graphQLValueProvider = (GraphQLValueProvider) provider;
                arguments.add(
                    GraphQLArgument.newArgument()
                        .name(graphQLValueProvider.getArgumentName())
                        .description(graphQLValueProvider.getDescription())
                        .defaultValue(graphQLValueProvider.getDefaultValue())
                        .type(graphQLValueProvider.getInputType())
                        .build()
                );
            }
        }
        return arguments;
    }

    public RelationConfiguration getRelationConfiguration(ForeignKey<?, ?> fk)
    {
        final RelationConfiguration relationConfiguration = relationConfigurations.get(fk);
        return relationConfiguration != null ? relationConfiguration : defaultRelationConfiguration;
    }


    public static DomainQLBuilder newDomainQL(DSLContext dslContext)
    {
        return new DomainQLBuilder(dslContext);
    }


    public void register(GraphQLSchema.Builder builder)
    {
        final Set<Class<?>> jooqInputTypes = new LinkedHashSet<>();
        jooqTables.forEach(table -> defineTypeForTable(builder, table, jooqTables, jooqInputTypes));

        final Map<Class<?>, String> map = new HashMap<>();


        jooqInputTypes.forEach( cls -> map.put(cls, cls.getSimpleName() + "Input"));
        inputTypes.forEach( cls -> {

            final String name = cls.getSimpleName();

            map.entrySet().removeIf(e -> e.getValue().equals(name));
            map.put(cls, name);

        });

        registerLogic(builder, logicBeans, map);

        defineInputTypes(builder, map);

    }


    private void defineInputTypes(GraphQLSchema.Builder builder, Map<Class<?>, String> typeToName)
    {
        for (Map.Entry<Class<?>, String> e : typeToName.entrySet())
        {
            final Class<?> type = e.getKey();
            final String name = e.getValue();

            log.debug("INPUT TYPE {} {}", name, type.getSimpleName());

            final JSONClassInfo classInfo = JSONUtil.getClassInfo(type);


            final GraphQLInputObjectType.Builder inputBuilder = GraphQLInputObjectType.newInputObject()
                .name(name)
                .description("Generated for " + type.getName());


            for (JSONPropertyInfo info : classInfo.getPropertyInfos())
            {
                final GraphQLInput inputFieldAnno = JSONUtil.findAnnotation(info, GraphQLInput.class);
                final Class<Object> propertyType = info.getType();
                GraphQLInputType inputType = getGraphQLScalarFor(propertyType);
                if (inputType == null)
                {
                    final String inputTypeName = typeToName.get(propertyType);

                    if (inputTypeName == null)
                    {
                        throw new IllegalStateException(type.getSimpleName() + "." + info.getJavaPropertyName() + ": Cannot find input type for " + propertyType.getName());
                    }
                    else
                    {
                        inputType = new GraphQLTypeReference(inputTypeName);
                    }
                }

                final Object defaultValueFromAnno = inputFieldAnno != null ? inputFieldAnno.defaultValue() : null;
                final Object defaultValue;
                if (defaultValueFromAnno == null || String.class.isAssignableFrom(propertyType))
                {
                    defaultValue = defaultValueFromAnno;
                }
                else
                {
                    defaultValue = ConvertUtils.convert(defaultValueFromAnno, propertyType);
                }

                final boolean jpaRequired = JSONUtil.findAnnotation(info, NotNull.class) != null;

                if (jpaRequired && inputFieldAnno != null && !inputFieldAnno.required())
                {
                    throw new DomainQLException(type.getSimpleName() + "." + info.getJavaPropertyName() +
                        ": Required field disagreement between @NotNull and @GraphQLInput required value");
                }

                final boolean isRequired = (inputFieldAnno != null && inputFieldAnno.required()) || jpaRequired;

                inputBuilder.field(
                    GraphQLInputObjectField.newInputObjectField()
                        .name(info.getJsonName())
                        .type(isRequired ? nonNull(inputType) : inputType)
                        .description(inputFieldAnno != null && inputFieldAnno.description().length() > 0 ? inputFieldAnno.description() : null)
                        .defaultValue(defaultValue)
                        .build()
                );
            }


            builder.additionalType(inputBuilder.build());
        }

    }



    private void defineTypeForTable(
        GraphQLSchema.Builder builder,
        Table<?> table, Set<Table<?>> tables,
        Set<Class<?>> jooqInputTypes
    )
    {
        try
        {

            final String typeName = table.getClass().getSimpleName();
            final Class<?> pojoType = findPojoTypeOf(table);

            final GraphQLObjectType.Builder domainTypeBuilder = GraphQLObjectType.newObject()
                .name(typeName);
            log.debug("DECLARE TYPE {}", typeName);

            final JSONClassInfo classInfo = JSONUtil.getClassInfo(pojoType);


            final Set<String> foreignKeyFields = findForeignKeyFields(table);

            for (JSONPropertyInfo info : classInfo.getPropertyInfos())
            {
                final Class<Object> type = info.getType();
                final Column jpaColumnAnno = getColumnAnnotation(pojoType, info);
                final boolean isRequired = JSONUtil.findAnnotation(info, NotNull.class) != null;

                if (foreignKeyFields.contains(jpaColumnAnno.name()))
                {
                    // ignore foreign key fields
                    continue;
                }

                final String name;
                final String jsonName = info.getJsonName();

                if (options.isUseDatabaseFieldNames())
                {
                    name = jpaColumnAnno.name();
                }
                else
                {
                    name = jsonName;
                }


                final GraphQLType graphQLType = getOutputType(type);
                if (graphQLType == null)
                {
                    throw new IllegalStateException("Could not determine graphql type for " + type);
                }

                final GraphQLFieldDefinition fieldDef;
                fieldDef = GraphQLFieldDefinition.newFieldDefinition()
                    .name(name)
                    .description("DB column '" + jpaColumnAnno.name() + "'")
                    .type(isRequired ? GraphQLNonNull.nonNull(graphQLType) : (GraphQLOutputType) graphQLType)
                    .dataFetcher(new SvensonFetcher(jsonName))
                    .build();

                log.debug("-- {}: {}", fieldDef.getName(), fieldDef.getType().getName());

                domainTypeBuilder.field(
                    fieldDef
                );
            }

            for (ForeignKey<?, ?> foreignKey : table.getReferences())
            {
                final List<? extends TableField<?, ?>> fields = foreignKey.getFields();


                // we ingore foreign keys that are configured with NONE and keys with more than one field
                final RelationConfiguration relationConfiguration = getRelationConfiguration(foreignKey);
                final SourceField sourceField = relationConfiguration.getSourceField();
                if (fields.size() != 1)
                {
                    continue;
                }
                final TableField<?, ?> foreignKeyField = fields.get(0);
                final JSONPropertyInfo fkPropertyInfo = findPropertyInfoForField(
                    table,
                    pojoType,
                    foreignKeyField
                );

                final boolean isRequired = JSONUtil.findAnnotation(fkPropertyInfo, NotNull.class) != null;

                final String javaName = fkPropertyInfo.getJavaPropertyName();
                if (javaName == null)
                {
                    throw new IllegalStateException("Cannot find java name for " + foreignKeyField);
                }


                switch (sourceField)
                {
                    case SCALAR:
                    {
                        final String scalarFieldName;
                        if (relationConfiguration.getLeftSideName() != null)
                        {
                            scalarFieldName = relationConfiguration.getLeftSideName();
                        }
                        else
                        {
                            scalarFieldName = javaName;
                        }

                        final GraphQLType graphQLType = getOutputType(foreignKeyField.getType());
                        if (graphQLType == null)
                        {
                            throw new IllegalStateException("Could not determine graphql type for " + foreignKeyField.getType());
                        }

                        final GraphQLFieldDefinition fieldDef = GraphQLFieldDefinition.newFieldDefinition()
                            .name(scalarFieldName)
                            .description("DB foreign key column '" + foreignKeyField.getName() + "'")
                            .type(isRequired ? GraphQLNonNull.nonNull(graphQLType) : (GraphQLOutputType) graphQLType)
                            .dataFetcher(new SvensonFetcher(findJsonName(classInfo, javaName)))
                            .build();

                        log.debug("-- fk scalar {}", fieldDef);

                        domainTypeBuilder.field(
                            fieldDef
                        );

                        break;
                    }
                    case OBJECT:
                    {
                        final String objectFieldName;
                        if (relationConfiguration.getLeftSideName() != null)
                        {
                            objectFieldName = relationConfiguration.getLeftSideName();
                        }
                        else
                        {
                            final String suffix = options.getForeignKeySuffix();
                            if (!javaName.endsWith(suffix))
                            {
                                throw new IllegalStateException(
                                    "Java property name does not end with configured foreign key suffix '" + suffix + "'");
                            }

                            objectFieldName = javaName.substring(0, javaName.length() - suffix.length());
                        }

                        final Class<?> otherPojoType = findPojoTypeOf(foreignKey.getKey().getTable());

                        final GraphQLTypeReference objectRef = new GraphQLTypeReference(otherPojoType.getSimpleName());
                        final GraphQLFieldDefinition fieldDef = GraphQLFieldDefinition.newFieldDefinition()
                            .name(objectFieldName)
                            .description("Target of '" + foreignKeyField.getName() + "'")
                            .type(isRequired ? GraphQLNonNull.nonNull(objectRef) : objectRef)
                            .dataFetcher(
                                new ReferenceFetcher(
                                    dslContext,
                                    findJsonName(classInfo, javaName),
                                    foreignKey.getKey().getTable(),
                                    findPojoTypeOf(foreignKey.getKey().getTable())
                                )
                            )
                            .build();

                        log.debug("-- fk target {}", fieldDef);

                        domainTypeBuilder.field(
                            fieldDef
                        );


                        break;
                    }
                    case NONE:
                        // nothing to do
                        break;

                }
            }

            final Set<ForeignKey<?, ?>> backReferences = findBackReferences(table, tables);

            for (ForeignKey<?, ?> foreignKey : backReferences)
            {
                if (foreignKey.getFields().size() != 1)
                {
                    continue;
                }

                final RelationConfiguration relationConfiguration = getRelationConfiguration(foreignKey);
                final TargetField targetField = relationConfiguration.getTargetField();
                final TableField<?, ?> foreignKeyField = foreignKey.getFields().get(0);

                final Table<?> otherTable = foreignKey.getTable();
                final Class<?> otherPojoType = findPojoTypeOf(otherTable);


                final boolean isOneToOne = targetField == TargetField.ONE;

                final GraphQLTypeReference type = new GraphQLTypeReference(otherPojoType.getSimpleName());

                final String backReferenceFieldName;
                if (relationConfiguration.getRightSideName() != null)
                {
                    backReferenceFieldName = relationConfiguration.getRightSideName();
                }
                else
                {
                    final String otherName = Introspector.decapitalize(otherPojoType.getSimpleName());
                    backReferenceFieldName = isOneToOne ? otherName : options.getPluralizationFunction().apply(otherName);
                }

                final JSONPropertyInfo fkPropertyInfo = findPropertyInfoForField(
                    table,
                    pojoType,
                    table.getPrimaryKey().getFields().get(0)
                );

                final boolean isRequired = JSONUtil.findAnnotation(fkPropertyInfo, NotNull.class) != null;

                final GraphQLOutputType fieldType = isOneToOne ? type : new GraphQLList(type);
                final GraphQLFieldDefinition fieldDef = GraphQLFieldDefinition.newFieldDefinition()
                    .name(backReferenceFieldName)
                    .description((isOneToOne ? "One-to-one object" : "Many-to-many objects") + " from '" + otherTable.getName() + "." + foreignKeyField.getName() + "'")
                    .type(isRequired ? GraphQLNonNull.nonNull(fieldType) : fieldType)
                    .dataFetcher(
                        new BackReferenceFetcher(
                            dslContext,
                            fkPropertyInfo.getJsonName(),
                            otherTable,
                            otherPojoType,
                            foreignKey,
                            isOneToOne
                        )
                    )
                    .build();


                log.debug("-- fk {} {}", isOneToOne ? "backref" : "backrefs", fieldDef);

                domainTypeBuilder.field(
                    fieldDef
                );
            }

            final GraphQLObjectType additionalType = domainTypeBuilder.build();
            builder.additionalType(additionalType);

            registeredOutputTypes.put(pojoType, additionalType);

            if (mirrorInputs)
            {
                jooqInputTypes.add(pojoType);
            }

        }
        catch (Exception e)
        {
            throw new DomainQLException("Error creating type for " + table, e);
        }
    }

}
