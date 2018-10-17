package de.quinscape.domainql;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.domainql.annotation.GraphQLFetcher;
import de.quinscape.domainql.annotation.GraphQLField;
import de.quinscape.domainql.config.Options;
import de.quinscape.domainql.config.RelationConfiguration;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.fetcher.BackReferenceFetcher;
import de.quinscape.domainql.fetcher.MethodFetcher;
import de.quinscape.domainql.fetcher.ReferenceFetcher;
import de.quinscape.domainql.fetcher.SvensonFetcher;
import de.quinscape.domainql.logic.DomainQLMethod;
import de.quinscape.domainql.logic.GraphQLValueProvider;
import de.quinscape.domainql.logic.Mutation;
import de.quinscape.domainql.logic.Query;
import de.quinscape.domainql.param.ParameterProvider;
import de.quinscape.domainql.param.ParameterProviderFactory;
import de.quinscape.domainql.schema.DomainQLAware;
import de.quinscape.domainql.util.DegenerificationUtil;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.introspection.Introspection;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLEnumType;
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
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.svenson.JSONProperty;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.info.JavaObjectPropertyInfo;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.schema.GraphQLNonNull.*;

/**
 * Annotation-based convention-over-configuration GraphQL Schema helper.
 */
public final class DomainQL
{
    private final static Logger log = LoggerFactory.getLogger(DomainQL.class);


    //private final static Map<String, GraphQLScalarType> NAME_TO_GRAPHQL;

    public static final String INPUT_SUFFIX = "Input";



    private final Collection<ParameterProviderFactory> parameterProviderFactories;

    private final Options options;

    private final DSLContext dslContext;

    private final Set<Object> logicBeans;

    private final Set<Table<?>> jooqTables;

    private final Map<ForeignKey<?, ?>, RelationConfiguration> relationConfigurations;

    private final RelationConfiguration defaultRelationConfiguration;

    private final Set<GraphQLFieldDefinition> additionalQueries;

    private final Set<GraphQLFieldDefinition> additionalMutations;

    private final Set<GraphQLDirective> additionalDirectives;

    private final Set<Class<?>> additionalInputTypes;

    private final boolean fullSupported;

    private final TypeRegistry typeRegistry;


    DomainQL(
        DSLContext dslContext,
        Set<Object> logicBeans,
        Set<Table<?>> jooqTables,
        Collection<ParameterProviderFactory> parameterProviderFactories,
        Map<ForeignKey<?, ?>, RelationConfiguration> relationConfigurations,
        RelationConfiguration defaultRelationConfiguration,
        Options options,
        Set<GraphQLFieldDefinition> additionalQueries,
        Set<GraphQLFieldDefinition> additionalMutations,
        Set<GraphQLDirective> additionalDirectives,
        Map<Class<?>, GraphQLScalarType> additionalScalarTypes,
        Set<Class<?>> additionalInputTypes,
        boolean fullSupported
    )
    {
        this.dslContext = dslContext;
        this.logicBeans = logicBeans;
        this.jooqTables = jooqTables;
        this.relationConfigurations = relationConfigurations;
        this.defaultRelationConfiguration = defaultRelationConfiguration;
        this.additionalQueries = additionalQueries;
        this.additionalMutations = additionalMutations;
        this.additionalDirectives = additionalDirectives;
        this.additionalInputTypes = additionalInputTypes;
        this.fullSupported = fullSupported;
        this.parameterProviderFactories = parameterProviderFactories;
        this.options = options;


        this.typeRegistry = new TypeRegistry(this, additionalScalarTypes);
    }




    public Set<GraphQLDirective> getAdditionalDirectives()
    {
        return additionalDirectives;
    }


    public static String getInputTypeName(Class<?> parameterType)
    {
        final String nameFromType = parameterType.getSimpleName();
        if (nameFromType.endsWith(INPUT_SUFFIX) || Enum.class.isAssignableFrom(parameterType))
        {
            return nameFromType;
        }
        else
        {
            return nameFromType + INPUT_SUFFIX;
        }
    }



    public static Class<?> findPojoTypeOf(Table<?> table)
    {
        try
        {
            final String typeName = table.getClass().getSimpleName();
            return Class.forName(
                // following jooq code generator conventions
                table.getClass().getPackage().getName() + ".pojos." + typeName
            );
        }
        catch (ClassNotFoundException e)
        {
            throw new DomainQLException(e);
        }
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


    /**
     * Tests if the user has imported the wrong of the same name accidentally by not importing the POJO class but
     * the Table or Record class.
     *
     * @param cls
     * @return
     */
    static Class<?> ensurePojoType(Class<?> cls)
    {
        if (!isPojoType(cls))
        {
            throw new DomainQLTypeException(cls.getName() + " is not a simple POJO class. Have you referenced the wrong class?");
        }

        return cls;
    }


    static boolean isPojoType(Class<?> cls)
    {
        return !Table.class.isAssignableFrom(cls) && !Record.class.isAssignableFrom(cls);
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
            if (info.isReadOnly())
            {
                continue;
            }

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


    private void definesLogicTypes(
        GraphQLSchema.Builder builder, LogicBeanAnalyzer analyzer, Collection<Object> logicBeans,
        Set<String> typesForJooqDomain
    )
    {
        log.debug("definesLogicTypes: logic beans = {}", logicBeans);

        for (OutputType outputType : typeRegistry.getOutputTypes())
        {
            final Class<?> javaType = outputType.getJavaType();
            if (typesForJooqDomain.contains(outputType.getName()) || outputType.isEnum() || typeRegistry.getGraphQLScalarFor(javaType, null) != null)
            {
                continue;
            }

            GraphQLObjectType.Builder domainTypeBuilder = GraphQLObjectType.newObject();
            domainTypeBuilder
                .name(outputType.getName())
                .description("Generated for " + outputType.getTypeContext().describe() );

            log.debug("DECLARE LOGIC TYPE {}", outputType.getName());

            buildFields(domainTypeBuilder, outputType, Collections.emptySet());

            final GraphQLObjectType newObjectType = domainTypeBuilder.build();
            builder.additionalType(newObjectType);

        }

        final Set<Query> queries = analyzer.getQueries();
        final Set<Mutation> mutations = analyzer.getMutations();

        final String logicBeanList = logicBeans.stream()
            .map(o -> AopProxyUtils.ultimateTargetClass(o).getSimpleName())
            .collect(Collectors.joining(", "));
        final GraphQLObjectType.Builder queryTypeBuilder = GraphQLObjectType.newObject()
            .name("QueryType")
            .description("Auto-generated from " + logicBeanList);

        final GraphQLObjectType.Builder mutationTypeBuilder = GraphQLObjectType.newObject()
            .name("MutationType")
            .description("Auto-generated from " + logicBeanList);

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

        additionalQueries.forEach(queryTypeBuilder::field);
        additionalMutations.forEach(mutationTypeBuilder::field);

        builder.query(queryTypeBuilder);
        builder.mutation(mutationTypeBuilder);
    }





    public static boolean isNormalProperty(JSONPropertyInfo info)
    {
        return !Class.class.isAssignableFrom(info.getType()) && ((JavaObjectPropertyInfo) info).getGetterMethod() != null;
    }


    public static GraphQLEnumType buildEnumType(Class<?> nextType)
    {
        final GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum()
            .name(nextType.getSimpleName());

        for (String value : getEnumValues(nextType))
        {
            enumBuilder.value(value);
        }
        ;

        return enumBuilder.build();
    }


    static  List<String> getEnumValues(Class<?> type)
    {
        Enum[] enums = null;
        try
        {
            enums = (Enum[]) type.getMethod("values").invoke(null);

            List<String> values = new ArrayList<>(enums.length);

            for (int i = 0; i < enums.length; i++)
            {
                Enum value = enums[i];
                values.add(value.name());
            }

            return values;
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            throw new DomainQLException(e);
        }


    }


    private List<GraphQLArgument> getGraphQLArguments(DomainQLMethod domainQLMethod)
    {
        List<GraphQLArgument> arguments = new ArrayList<>();
        for (ParameterProvider provider : domainQLMethod.getParameterProviders())
        {
            if (provider instanceof GraphQLValueProvider)
            {
                final GraphQLValueProvider graphQLValueProvider = (GraphQLValueProvider) provider;
                final GraphQLInputType inputType = graphQLValueProvider.getInputType();
                arguments.add(
                    GraphQLArgument.newArgument()
                        .name(graphQLValueProvider.getArgumentName())
                        .description(graphQLValueProvider.getDescription())
                        .defaultValue(graphQLValueProvider.getDefaultValue())
                        .type(
                            graphQLValueProvider.isNotNull() ? GraphQLNonNull.nonNull(inputType) : inputType
                        )
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


    protected void register(GraphQLSchema.Builder builder)
    {
        Set<String> typesForJooqDomain = new HashSet<>();
        final LogicBeanAnalyzer analyzer = registerTypes(builder, typesForJooqDomain);

        defineGraphQLTypes(builder, typesForJooqDomain, analyzer);
    }

    private void defineGraphQLTypes(
        GraphQLSchema.Builder builder, Set<String> typesForJooqDomain, LogicBeanAnalyzer analyzer
    )
    {
        definesLogicTypes(builder, analyzer, logicBeans, typesForJooqDomain);
        defineEnumTypes(builder);
        defineInputTypes(builder);

        builder.additionalDirectives(
            additionalDirectives
        );

        if (fullSupported)
        {
            builder.additionalDirective(
                GraphQLDirective.newDirective()
                    .name("full")
                    .description(
                        "Escape-hatch to make GraphQL get out of your way and return the complete DomainQL query or " +
                            "mutation response as-is with standard JSONification")
                    .validLocations(
                        Introspection.DirectiveLocation.FIELD
                    )
                    .build()
            );
        }

    }


    private LogicBeanAnalyzer registerTypes(GraphQLSchema.Builder builder, Set<String> typesForJooqDomain)
    {
        // define types for the JOOQ Tables
        for (Table<?> table : jooqTables)
        {
            final Class<?> pojoType = ensurePojoType(
                findPojoTypeOf(table)
            );

            typeRegistry.register(new TypeContext(null, pojoType));

            defineTypeForTable(builder, table, pojoType, typesForJooqDomain);
        }

        for (Class<?> inputType : additionalInputTypes)
        {
            typeRegistry.registerInput(new TypeContext(null, inputType));
        }

        return new LogicBeanAnalyzer(
            this,
            parameterProviderFactories,
            logicBeans,
            typeRegistry
        );
    }


    public boolean isFullSupported()
    {
        return fullSupported;
    }


    private void defineEnumTypes(GraphQLSchema.Builder builder)
    {
        final Set<? extends Class<?>> enumTypes = Stream.concat(
            typeRegistry.getInputTypes().stream(),
            typeRegistry.getOutputTypes().stream()
        )
            .filter(ComplexType::isEnum)
            .map(ComplexType::getJavaType)
            // remove duplicates
            .collect(Collectors.toSet());

        for (Class<?> enumType : enumTypes)
        {
            final GraphQLEnumType graphQLEnumType = DomainQL.buildEnumType(enumType);

            log.debug("DECLARE ENUM TYPE -- {}", enumType);

            builder.additionalType(
                graphQLEnumType
            );
        }

    }
    /**
     * Builds all input types
     *  @param builder       GraphQL schema builder
     *
     */
    private void defineInputTypes(GraphQLSchema.Builder builder)
    {

        for (InputType inputType : typeRegistry.getInputTypes())
        {
            final Class<?> javaType = ensurePojoType(inputType.getJavaType());

            final TypeContext typeContext = inputType.getTypeContext();

            final String name = inputType.getName();

            if (javaType.isEnum())
            {
                continue;
            }
            
            log.debug("INPUT TYPE {} {}", name, javaType.getSimpleName());

            final JSONClassInfo classInfo = JSONUtil.getClassInfo(javaType);

            final GraphQLInputObjectType.Builder inputBuilder = GraphQLInputObjectType.newInputObject()
                .name(name)
                .description("Generated for " + javaType.getName());

            for (JSONPropertyInfo info : classInfo.getPropertyInfos())
            {
                final Method getterMethod = ((JavaObjectPropertyInfo) info).getGetterMethod();

                final GraphQLField inputFieldAnno = JSONUtil.findAnnotation(info, GraphQLField.class);
                final Class<?> propertyType = info.getType();
                GraphQLInputType graphQLFieldType = typeRegistry.getGraphQLScalarFor(propertyType, inputFieldAnno);
                if (graphQLFieldType == null)
                {
                    if (List.class.isAssignableFrom(propertyType))
                    {
                        graphQLFieldType = (GraphQLInputType) getListType(typeContext, javaType, info, false);
                    }
                    else if (Enum.class.isAssignableFrom(propertyType))
                    {
                        graphQLFieldType = new GraphQLTypeReference(propertyType.getSimpleName());
                    }
                    else
                    {
                        final TypeContext fieldCtx = DegenerificationUtil.getType(inputType.getTypeContext(), inputType, getterMethod);
                        final InputType fieldType = typeRegistry.lookupInput(fieldCtx);
                        if (fieldType == null)
                        {
                            throw new IllegalStateException("Could not find input type for " + propertyType + ", ctx = " + fieldCtx);
                        }
                        graphQLFieldType = typeRef(fieldType.getName());
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

                final boolean jpaNotNull = JSONUtil.findAnnotation(info, NotNull.class) != null;

                if (jpaNotNull && inputFieldAnno != null && !inputFieldAnno.notNull())
                {
                    throw new DomainQLException(javaType.getSimpleName() + "." + info.getJavaPropertyName() +
                        ": Required field disagreement between @NotNull and @GraphQLField required value");
                }

                final boolean isNotNull = (inputFieldAnno != null && inputFieldAnno.notNull()) || jpaNotNull;

                inputBuilder.field(
                    GraphQLInputObjectField.newInputObjectField()
                        .name(inputFieldAnno != null && inputFieldAnno.value().length() > 0 ? inputFieldAnno.value() : info.getJsonName())
                        .type(isNotNull ? nonNull(graphQLFieldType) : graphQLFieldType)
                        .description(inputFieldAnno != null && inputFieldAnno.description().length() > 0 ? inputFieldAnno.description() : null)
                        .defaultValue(defaultValue)
                        .build()
                );
            }
            builder.additionalType(inputBuilder.build());
        }
    }


    private GraphQLInputType typeRef(String name)
    {
        log.debug("typeRef {}", name);

        return new GraphQLTypeReference(name);
    }


    private GraphQLInputType inputTypeRef(Class<?> propertyType)
    {
        if (propertyType.equals(Object.class))
        {
            throw new IllegalStateException();
        }

        final GraphQLScalarType type = typeRegistry.getGraphQLScalarFor(propertyType, (GraphQLField) null);
        if (type != null)
        {
            return type;
        }
        else
        {
            return GraphQLTypeReference.typeRef(getInputTypeName(propertyType));
        }
    }

    private GraphQLOutputType outputTypeRef(Class<?> propertyType)
    {

        if (propertyType.equals(Object.class))
        {
            throw new IllegalStateException();
        }

        final GraphQLScalarType type = typeRegistry.getGraphQLScalarFor(propertyType, null);
        if (type != null)
        {
            return type;
        }
        else
        {
            final String name = propertyType.getSimpleName();
            log.debug("outputTypeRef: {}", name);

            return GraphQLTypeReference.typeRef(name);
        }
    }


    private GraphQLType getListType(TypeContext outputType,Class<?> type, JSONPropertyInfo info, boolean isOutputType)
    {
        final Method getterMethod = ((JavaObjectPropertyInfo) info).getGetterMethod();
        final String propertyName = info.getJavaPropertyName();

        return getListType(outputType, type, getterMethod, propertyName, isOutputType);
    }


    private GraphQLType getListType(TypeContext outputType, Class<?> type, Method getterMethod, String propertyName, boolean isOutputType)
    {
        GraphQLType inputType;
        final Type genericReturnType = getterMethod.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType))
        {
            throw new DomainQLException(type.getName() + "." + propertyName +
                ": Property getter type must be parametrized.");
        }

        final Type actualTypeArgument = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];

        final Class<?> elementClass;
        if (actualTypeArgument instanceof TypeVariable)
        {
            elementClass = outputType.resolveType(((TypeVariable) actualTypeArgument).getName());
            if (elementClass == null)
            {
                throw new IllegalStateException("Cannot resolve " + actualTypeArgument);
            }
        }                                                                                   
        else
        {
            elementClass = (Class<?>) actualTypeArgument;
        }

        inputType = isOutputType ? new GraphQLList(outputTypeRef(elementClass)) : new GraphQLList(inputTypeRef(elementClass));
        return inputType;
    }


    private void defineTypeForTable(
        GraphQLSchema.Builder builder,
        Table<?> table,
        Class<?> pojoType,
        Set<String> typesForJooqDomain
    )
    {
        try
        {

            final String typeName = table.getClass().getSimpleName();

            final javax.persistence.Table tableAnno = pojoType.getAnnotation(javax.persistence.Table.class);
            final GraphQLObjectType.Builder domainTypeBuilder = GraphQLObjectType.newObject()
                .name(typeName)
                .description(tableAnno != null ? "Generated from " + tableAnno.schema() + "." + table.getName() : null);
            log.debug("DECLARE TYPE {}", typeName);

            final JSONClassInfo classInfo = JSONUtil.getClassInfo(pojoType);


            final Set<String> foreignKeyFields = findForeignKeyFields(table);

            final OutputType outputType = typeRegistry.lookup(pojoType);

            if (outputType == null)
            {
                throw new IllegalStateException("Could not find output type for type " + pojoType.getName());
            }

            buildFields(domainTypeBuilder, outputType, foreignKeyFields);

            buildForeignKeyFields(domainTypeBuilder, pojoType, classInfo, table);

            buildBackReferenceFields(domainTypeBuilder, pojoType, table, jooqTables);

            final GraphQLObjectType newObjectType = domainTypeBuilder.build();


            builder.additionalType(newObjectType);

            typesForJooqDomain.add(outputType.getName());
        }
        catch (Exception e)
        {
            throw new DomainQLException("Error creating type for " + table, e);
        }
    }


    /**
     * Build all fields resulting from a foreign key pointing to the current object type.
     *
     * @param domainTypeBuilder     object builder
     * @param pojoType              pojo type to build the object for
     * @param table                 corresponding table
     * @param allTables                set of all tables
     */
    private void buildBackReferenceFields(
        GraphQLObjectType.Builder domainTypeBuilder,
        Class<?> pojoType,
        Table<?> table,
        Set<Table<?>> allTables
    )
    {
        final Set<ForeignKey<?, ?>> backReferences = findBackReferences(table, allTables);

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

            final GraphQLOutputType type = outputTypeRef(otherPojoType);

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

            final boolean isNotNull = JSONUtil.findAnnotation(fkPropertyInfo, NotNull.class) != null;

            final GraphQLOutputType fieldType = isOneToOne ? type : new GraphQLList(type);
            final GraphQLFieldDefinition fieldDef = GraphQLFieldDefinition.newFieldDefinition()
                .name(backReferenceFieldName)
                .description((isOneToOne ? "One-to-one object" : "Many-to-many objects") + " from '" + otherTable.getName() + "." + foreignKeyField.getName() + "'")
                .type(isNotNull ? GraphQLNonNull.nonNull(fieldType) : fieldType)
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
    }


    /**
     * Build the fields resulting from the foreign keys of this type.
     *
     * @param domainTypeBuilder     object builder
     * @param pojoType              pojo type to build the object for
     * @param classInfo             JSON classInfo for that type
     * @param table                 corresponding table
     */
    private void buildForeignKeyFields(
        GraphQLObjectType.Builder domainTypeBuilder,
        Class<?> pojoType,
        JSONClassInfo classInfo,
        Table<?> table
    )
    {
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

            final boolean isNotNull = JSONUtil.findAnnotation(fkPropertyInfo, NotNull.class) != null;

            final String javaName = fkPropertyInfo.getJavaPropertyName();
            if (javaName == null)
            {
                throw new IllegalStateException("Cannot find java name for " + foreignKeyField);
            }


            if (sourceField == SourceField.SCALAR || sourceField == SourceField.OBJECT_AND_SCALAR)
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
                final GraphQLType graphQLType = outputTypeRef(foreignKeyField.getType());
                if (graphQLType == null)
                {
                    throw new IllegalStateException(
                        "Could not determine graphql type for " + foreignKeyField.getType());
                }
                final GraphQLFieldDefinition fieldDef = GraphQLFieldDefinition.newFieldDefinition()
                    .name(scalarFieldName)
                    .description("DB foreign key column '" + foreignKeyField.getName() + "'")
                    .type(isNotNull ? GraphQLNonNull.nonNull(graphQLType) : (GraphQLOutputType) graphQLType)
                    .dataFetcher(new SvensonFetcher(findJsonName(classInfo, javaName)))
                    .build();
                log.debug("-- fk scalar {}", fieldDef);
                domainTypeBuilder.field(
                    fieldDef
                );
            }

            if (sourceField == SourceField.OBJECT || sourceField == SourceField.OBJECT_AND_SCALAR)
            {
                final String objectFieldName;
                if (relationConfiguration.getLeftSideName() != null)
                {
                    objectFieldName = relationConfiguration.getLeftSideName();
                }
                else
                {
                    final String suffix = options.getForeignKeySuffix();
                    if (javaName.endsWith(suffix))
                    {
                        objectFieldName = javaName.substring(0, javaName.length() - suffix.length());
                    }
                    else
                    {
                        objectFieldName = javaName;
                    }

                }
                final Class<?> otherPojoType = findPojoTypeOf(foreignKey.getKey().getTable());
                final GraphQLOutputType objectRef = outputTypeRef(otherPojoType);
                final GraphQLFieldDefinition fieldDef = GraphQLFieldDefinition.newFieldDefinition()
                    .name(objectFieldName)
                    .description("Target of '" + foreignKeyField.getName() + "'")
                    .type(isNotNull ? GraphQLNonNull.nonNull(objectRef) : objectRef)
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
            }
            else if (sourceField == SourceField.NONE)
            {// nothing to do

            }
        }
    }


    /**
     * Builds the normal fields for the given type.
     * @param outputType                   output type reference
     * @param foreignKeyFields      Names of fields that are part of a foreign keys
     *
     */
    private void buildFields(
        GraphQLObjectType.Builder domainTypeBuilder,
        OutputType outputType,
        Set<String> foreignKeyFields
    )
    {
        final Class<?> javaType = outputType.getJavaType();

        final JSONClassInfo classInfo = JSONUtil.getClassInfo(javaType);

        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {

            if (!isNormalProperty(info))
            {
                continue;
            }

            final Class<Object> type = info.getType();
            final Column jpaColumnAnno = JSONUtil.findAnnotation(info, Column.class);
            final GraphQLField fieldAnno = JSONUtil.findAnnotation(info, GraphQLField.class);
            final GraphQLFetcher fetcherAnno = JSONUtil.findAnnotation(info, GraphQLFetcher.class);

            final Method getterMethod = ((JavaObjectPropertyInfo) info).getGetterMethod();


            if (options.isUseDatabaseFieldNames() && jpaColumnAnno == null)
            {
                throw new DomainQLException(type.getSimpleName() + "." + info.getJavaPropertyName() + ": Missing @Column annotation");
            }

            final boolean isNotNull = JSONUtil.findAnnotation(info, NotNull.class) != null;

            if (jpaColumnAnno != null && foreignKeyFields.contains(jpaColumnAnno.name()))
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

            GraphQLType graphQLType;
            if (List.class.isAssignableFrom(type))
            {
                graphQLType = getListType(outputType.getTypeContext(), type, info, true);
            }
            else
            {
                final GraphQLScalarType scalarType = typeRegistry.getGraphQLScalarFor(type, fieldAnno);
                if (scalarType != null)
                {
                    graphQLType = scalarType;
                }
                else
                {
                    graphQLType = new GraphQLTypeReference(DegenerificationUtil.getType(outputType.getTypeContext(), outputType, getterMethod).getTypeName());
                }
            }

            final GraphQLFieldDefinition fieldDef;
            fieldDef = GraphQLFieldDefinition.newFieldDefinition()
                .name(fieldAnno != null && fieldAnno.value().length() > 0 ? fieldAnno.value() : name)
                .description(
                    fieldAnno != null && fieldAnno.description().length() > 0 ?
                        fieldAnno.description() :
                        jpaColumnAnno != null ?
                            "DB column '" + jpaColumnAnno.name() + "'" :
                            type.getSimpleName() + "." + jsonName
                )
                .type(isNotNull ? GraphQLNonNull.nonNull(graphQLType) : (GraphQLOutputType) graphQLType)
                .dataFetcher(fetcherAnno == null ? new SvensonFetcher(jsonName) : createFetcher(fetcherAnno.value(), fetcherAnno.data(), jsonName))
                .build();

            log.debug("-- {}: {}", fieldDef.getName(), fieldDef.getType().getName());

            domainTypeBuilder.field(
                fieldDef
            );
        }

        MethodAccess methodAccess = null;


        for (Method m : javaType.getMethods())
        {
            Class<?>[] parameterTypes = m.getParameterTypes();
            final GraphQLField fieldAnno = m.getAnnotation(GraphQLField.class);
            if (fieldAnno != null && parameterTypes.length > 0)
            {
                if (methodAccess == null)
                {
                    methodAccess = MethodAccess.get(javaType);
                }

                final int methodIndex = methodAccess.getIndex(m.getName(), parameterTypes);

                final String propertyName = getPropertyName(m);
                final StringBuilder paramDesc = new StringBuilder();
                for (int i = 0; i < parameterTypes.length; i++)
                {
                    Class<?> parameterType = parameterTypes[i];
                    if (i > 0)
                    {
                        paramDesc.append(",");
                    }
                    paramDesc.append(parameterType.getSimpleName());
                }

                final boolean isNotNull = m.getAnnotation(NotNull.class) != null;

                final Class<?> returnType = m.getReturnType();

                GraphQLType graphQLType;
                if (List.class.isAssignableFrom(returnType))
                {
                    graphQLType = getListType(outputType.getTypeContext(), (Class<?>) returnType, m, propertyName, true);
                }
                else
                {
                    final GraphQLScalarType scalarType = typeRegistry.getGraphQLScalarFor(returnType, fieldAnno);
                    if (scalarType != null)
                    {
                        graphQLType = scalarType;
                    }
                    else
                    {
                        graphQLType = outputTypeRef(returnType);
                    }
                }

                List<String> parameterNames = new ArrayList<>();
                for (Parameter parameter : m.getParameters())
                {
                    if (!parameter.isNamePresent())
                    {
                        throw new IllegalStateException("No method parameter names provided by compiler");
                    }
                    parameterNames.add(
                        parameter.getName()
                    );
                }


                final GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                    .name(fieldAnno.value().length() > 0 ? fieldAnno.value() : propertyName)
                    .description(
                        fieldAnno.description().length() > 0 ?
                            fieldAnno.description() :
                            javaType.getSimpleName() + "." + m.getName() + "(" + paramDesc.toString() + ")"
                    )
                    .type(isNotNull ? GraphQLNonNull.nonNull(graphQLType) : (GraphQLOutputType) graphQLType)
                    .dataFetcher(new MethodFetcher(methodAccess, methodIndex, parameterNames, parameterTypes));

                for (Parameter parameter : m.getParameters())
                {
                    final Class<?> parameterType = parameter.getType();
                    final GraphQLField paramFieldAnno = parameter.getAnnotation(GraphQLField.class);

                    GraphQLInputType paramGQLType;
                    if (List.class.isAssignableFrom(parameterType))
                    {

                        GraphQLType inputType;
                        final Type genericParamType = parameter.getParameterizedType();
                        if (!(genericParamType instanceof ParameterizedType))
                        {
                            throw new DomainQLException(parameterType.getName() + "." + propertyName +
                                ": Property getter type must be parametrized.");
                        }

                        final Class<?> elementClass = (Class<?>) ((ParameterizedType) genericParamType).getActualTypeArguments()[0];

                        paramGQLType = new GraphQLList(inputTypeRef(elementClass));
                    }
                    else
                    {
                        final GraphQLScalarType scalarType = typeRegistry.getGraphQLScalarFor(parameterType, paramFieldAnno);
                        if (scalarType != null)
                        {
                            paramGQLType = scalarType;
                        }
                        else
                        {
                            paramGQLType = inputTypeRef(parameterType);
                        }
                    }

                    final GraphQLArgument.Builder arg = GraphQLArgument.newArgument()
                        .name(parameter.getName())
                        .defaultValue(paramFieldAnno != null ? paramFieldAnno.defaultValue() : null)
                        .type(
                            paramFieldAnno != null && paramFieldAnno.notNull() ? GraphQLNonNull
                                .nonNull(paramGQLType) : paramGQLType
                        )
                        ;

                    log.debug("Method Argument {}: {}", parameter.getName(), paramGQLType);

                    fieldBuilder.argument(
                        arg.build()
                    );

                }


                final GraphQLFieldDefinition fieldDef = fieldBuilder
                    .build();

                log.debug("-- {}: {}", fieldDef.getName(), fieldDef.getType().getName());

                domainTypeBuilder.field(
                    fieldDef
                );

            }
        }
    }


    private static String getPropertyName(Method m)
    {
        final JSONProperty annotation = m.getAnnotation(JSONProperty.class);
        if (annotation != null && annotation.value().length() > 0)
        {
            return annotation.value();
        }

        final boolean isGetter = m.getName().startsWith("get");
        final boolean isIsser = m.getName().startsWith("is");
        if (!isGetter && !isIsser)
        {
            throw new IllegalStateException("Parametrized getter name must start with 'get' or 'is'");
        }

        return Introspector.decapitalize(m.getName().substring(isGetter ? 3 : 2));
    }


    private DataFetcher<?> createFetcher(Class<? extends DataFetcher> cls, String data, String jsonName)
    {

        if (!DataFetcher.class.isAssignableFrom(cls))
        {
            throw new DomainQLException(cls + " does not implement" + DataFetcher.class.getName());
        }

        final String className = cls.getName();
        try
        {
            final Constructor<?>[] constructors = cls.getConstructors();

            if (constructors.length > 1)
            {
                throw new DomainQLException("Fetcher can only have one constructor");
            }

            final Constructor<?> ctor = constructors[0];

            final Class<?>[] parameterTypes = ctor.getParameterTypes();
            if (parameterTypes.length > 2)
            {
                throw new DomainQLException("Fetcher constructor can take 2 most two parmeters");
            }
            for (Class<?> type : parameterTypes)
            {
                if (!type.equals(String.class))
                {
                    throw new DomainQLException("Fetcher constructor can take only String args: (name) or (name,data)");
                }
            }

            if (parameterTypes.length == 0)
            {
                return (DataFetcher<?>)ctor.newInstance();
            }
            else if (parameterTypes.length == 1)
            {
                return (DataFetcher<?>)ctor.newInstance(jsonName);
            }
            else
            {
                return (DataFetcher<?>)ctor.newInstance(jsonName, data);
            }
        }
        catch (IllegalAccessException e)
        {
            throw new DomainQLException("Cannot access contructor" + className + "(String,String).", e);
        }
        catch (InstantiationException e)
        {
            throw new DomainQLException("Cannot instantiate " + className , e);
        }
        catch (InvocationTargetException e)
        {
            throw new DomainQLException("Error instantiating " + className , e.getTargetException());
        }
    }


    public Options getOptions()
    {
        return options;
    }


    public Set<Object> getLogicBeans()
    {
        return logicBeans;
    }


    public Set<Table<?>> getJooqTables()
    {
        return jooqTables;
    }


    public Map<ForeignKey<?, ?>, RelationConfiguration> getRelationConfigurations()
    {
        return relationConfigurations;
    }


    public RelationConfiguration getDefaultRelationConfiguration()
    {
        return defaultRelationConfiguration;
    }


    public Set<GraphQLFieldDefinition> getAdditionalQueries()
    {
        return additionalQueries;
    }


    public Set<GraphQLFieldDefinition> getAdditionalMutations()
    {
        return additionalMutations;
    }


    public TypeRegistry getTypeRegistry()
    {
        return typeRegistry;
    }



    void register(GraphQLSchema schema)
    {
        for (GraphQLScalarType scalarType : getTypeRegistry().getScalarTypes())
        {
            if (scalarType instanceof DomainQLAware)
            {
                ((DomainQLAware) scalarType).registerSchema(this, schema);
            }
        }
    }
}


