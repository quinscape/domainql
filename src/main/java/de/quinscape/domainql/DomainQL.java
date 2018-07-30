package de.quinscape.domainql;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.quinscape.domainql.annotation.GraphQLFetcher;
import de.quinscape.domainql.annotation.GraphQLField;
import de.quinscape.domainql.annotation.GraphQLObject;
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
import de.quinscape.domainql.logic.LogicBeanAnalyzer;
import de.quinscape.domainql.logic.Mutation;
import de.quinscape.domainql.logic.Query;
import de.quinscape.domainql.param.ParameterProvider;
import de.quinscape.domainql.param.ParameterProviderFactory;
import de.quinscape.domainql.scalar.GraphQLCurrencyScalar;
import de.quinscape.domainql.scalar.GraphQLDateScalar;
import de.quinscape.domainql.scalar.GraphQLTimestampScalar;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.Scalars;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
import java.util.stream.Collectors;

import static graphql.schema.GraphQLNonNull.*;

/**
 * Annotation-based convention-over-configuration GraphQL Schema helper.
 */
public final class DomainQL
{
    private final static Logger log = LoggerFactory.getLogger(DomainQL.class);

    private final static Map<Class<?>, GraphQLScalarType> JAVA_TYPE_TO_GRAPHQL;
    private final static Map<String, GraphQLScalarType> NAME_TO_GRAPHQL;

    public static final String INPUT_SUFFIX = "Input";

    static
    {
        final Map<Class<?>, GraphQLScalarType> map = new HashMap<>();

        map.put(Boolean.class, Scalars.GraphQLBoolean);
        map.put(Boolean.TYPE, Scalars.GraphQLBoolean);
        map.put(Byte.class, Scalars.GraphQLByte);
        map.put(Byte.TYPE, Scalars.GraphQLByte);
        map.put(Short.class, Scalars.GraphQLShort);
        map.put(Short.TYPE, Scalars.GraphQLShort);
        map.put(Integer.class, Scalars.GraphQLInt);
        map.put(Integer.TYPE, Scalars.GraphQLInt);
        map.put(Double.class, Scalars.GraphQLFloat);
        map.put(Double.TYPE, Scalars.GraphQLFloat);
        map.put(Long.class, Scalars.GraphQLLong);
        map.put(Long.TYPE, Scalars.GraphQLLong);
        map.put(String.class, Scalars.GraphQLString);
        map.put(BigDecimal.class, Scalars.GraphQLBigDecimal);
        map.put(BigInteger.class, Scalars.GraphQLBigInteger);
        map.put(Timestamp.class, new GraphQLTimestampScalar());
        map.put(Date.class, new GraphQLDateScalar());

        JAVA_TYPE_TO_GRAPHQL = Collections.unmodifiableMap(map);
    }

    static
    {
        final Map<String, GraphQLScalarType> map = new HashMap<>();

        final Collection<GraphQLScalarType> scalarTypes = new ArrayList<>(JAVA_TYPE_TO_GRAPHQL.values());

        // types only available by name
        scalarTypes.add(new GraphQLCurrencyScalar());

        for (GraphQLScalarType scalarType : scalarTypes)
        {
            map.put(scalarType.getName(), scalarType);
        }

        NAME_TO_GRAPHQL = Collections.unmodifiableMap(map);
    }

    private final Collection<ParameterProviderFactory> parameterProviderFactories;

    private final Options options;

    private final Map<Class<?>, GraphQLOutputType> registeredOutputTypes;

    private final Map<Class<?>, GraphQLInputType> registeredInputTypes;

    private final DSLContext dslContext;

    private final Set<Object> logicBeans;

    private final Set<Table<?>> jooqTables;

    private final boolean mirrorInputs;

    private final Set<Class<?>> inputTypes;

    private final Map<ForeignKey<?, ?>, RelationConfiguration> relationConfigurations;

    private final RelationConfiguration defaultRelationConfiguration;

    private final Set<GraphQLFieldDefinition> additionalQueries;

    private final Set<GraphQLFieldDefinition> additionalMutations;

    private final Set<GraphQLDirective> additionalDirectives;


    DomainQL(
        DSLContext dslContext,
        Set<Object> logicBeans,
        Set<Table<?>> jooqTables,
        Set<Class<?>> inputTypes,
        Collection<ParameterProviderFactory> parameterProviderFactories,
        Map<ForeignKey<?, ?>, RelationConfiguration> relationConfigurations,
        RelationConfiguration defaultRelationConfiguration,
        Options options,
        boolean mirrorInputs,
        Set<GraphQLFieldDefinition> additionalQueries,
        Set<GraphQLFieldDefinition> additionalMutations,

        Set<GraphQLDirective> additionalDirectives
    )
    {
        this.dslContext = dslContext;
        this.logicBeans = logicBeans;
        this.jooqTables = jooqTables;
        this.mirrorInputs = mirrorInputs;
        this.inputTypes = inputTypes;
        this.relationConfigurations = relationConfigurations;
        this.defaultRelationConfiguration = defaultRelationConfiguration;
        this.additionalQueries = additionalQueries;
        this.additionalMutations = additionalMutations;
        this.additionalDirectives = additionalDirectives;

        registeredOutputTypes = new HashMap<>(JAVA_TYPE_TO_GRAPHQL);
        registeredInputTypes = new HashMap<>(JAVA_TYPE_TO_GRAPHQL);

        this.parameterProviderFactories = parameterProviderFactories;
        this.options = options;
    }


    public Set<GraphQLDirective> getAdditionalDirectives()
    {
        return additionalDirectives;
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
    public static GraphQLScalarType getGraphQLScalarFor(Class<?> cls, GraphQLField inputAnno)
    {
        if (inputAnno != null && inputAnno.type().length() > 0)
        {
            return NAME_TO_GRAPHQL.get(inputAnno.type());
        }
        return JAVA_TYPE_TO_GRAPHQL.get(cls);
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


    /**
     * Returns the output type registered for the given pojo type.
     *
     * @param cls
     * @return
     */
    public GraphQLOutputType getOutputType(Class<?> cls)
    {
        return registeredOutputTypes.get(cls);
    }

    public GraphQLInputType getInputType(Class<?> cls)
    {
        return registeredInputTypes.get(cls);
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

    private Class<?> ensurePojoType(Class<?> cls)
    {
        if (Table.class.isAssignableFrom(cls) || Record.class.isAssignableFrom(cls))
        {
            throw new DomainQLTypeException(cls.getName() + " is not a simple POJO class. Have you referenced the wrong class?");
        }

        return cls;
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
        GraphQLSchema.Builder builder, Collection<Object> logicBeans, BiMap<Class<?>, String> inputTypes
    )
    {
        log.debug("registerLogic {}", logicBeans);

        final Set<Class<?>> extraOutputTypes = new HashSet<>();

        final LogicBeanAnalyzer analyzer = new LogicBeanAnalyzer(
            this,
            parameterProviderFactories,
            logicBeans,
            inputTypes,
            registeredOutputTypes,
            extraOutputTypes::add
        );

        // copy found types to prevent concurrent modification exception
        final Set<Class<?>> copy = new HashSet<>(extraOutputTypes);
        for (Class<?> outputType : copy)
        {
            addOutputTypesForFields(builder, outputType, extraOutputTypes);
        }

        for (Class<?> outputType : extraOutputTypes)
        {
            GraphQLObjectType.Builder domainTypeBuilder = GraphQLObjectType.newObject();
            domainTypeBuilder
                .name(outputType.getSimpleName())
                .description("Generated for " + outputType.getName() );

            log.debug("DECLARE TYPE {}", outputType.getSimpleName());

            buildFields(domainTypeBuilder, outputType, Collections.emptySet(), extraOutputTypes);

            final GraphQLObjectType newObjectType = domainTypeBuilder.build();
            builder.additionalType(newObjectType);


            final GraphQLObject objectAnno = outputType.getAnnotation(GraphQLObject.class);
            if (mirrorInputs && (objectAnno == null || objectAnno.createMirror()))
            {
                inputTypes.put(outputType, outputType.getSimpleName() + INPUT_SUFFIX);
            }

            registeredOutputTypes.put(outputType, newObjectType);
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


    private void addOutputTypesForFields(
        GraphQLSchema.Builder builder, Class<?> outputType, Set<Class<?>> extraOutputTypes
    )
    {
        if (registeredOutputTypes.containsKey(outputType))
        {
            // already registered
            return;
        }

        extraOutputTypes.add(outputType);

        final JSONClassInfo classInfo = JSONUtil.getClassInfo(outputType);
        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {
            final Class<Object> type = info.getType();

            final GraphQLField graphQLFieldAnno = JSONUtil.findAnnotation(info, GraphQLField.class);

            if (info.getJavaPropertyName().equals("class") || type.equals(Object.class))
            {
                continue;
            }

            final Class<?> nextType;
            if (List.class.isAssignableFrom(type))
            {
                final Method getterMethod = ((JavaObjectPropertyInfo) info).getGetterMethod();

                final Type genericReturnType = getterMethod.getGenericReturnType();
                if (!(genericReturnType instanceof ParameterizedType))
                {
                    throw new DomainQLException(type.getName() + "." + info.getJavaPropertyName() + ": Property getter type must be parametrized.");
                }

                final Type actualType = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
                if (actualType instanceof Class)
                {
                    nextType = (Class<?>) actualType;
                }
                else
                {
                    throw new DomainQLException("Error getting generic type for" + actualType + " / " + getterMethod);
                }
            }
            else
            {
                nextType = type;
            }

            if (Enum.class.isAssignableFrom(nextType))
            {
                final GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum()
                    .name(nextType.getSimpleName());

                for (String value : getEnumValues(nextType))
                {
                    enumBuilder.value(value);
                };

                builder.additionalType(enumBuilder.build());

            }
            else if (DomainQL.getGraphQLScalarFor(nextType, graphQLFieldAnno) == null && !extraOutputTypes.contains(nextType) && !nextType.equals(Object.class))
            {
                log.debug("From {}.{} visit {}", outputType.getSimpleName(), info.getJavaPropertyName(), nextType);

                addOutputTypesForFields(builder, nextType, extraOutputTypes);
            }
        }

        for (Method method : outputType.getMethods())
        {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            final GraphQLField annotation = method.getAnnotation(GraphQLField.class);
            if (parameterTypes.length > 0 && annotation != null)
            {
                addOutputTypesForFields(builder, method.getReturnType(), extraOutputTypes);
            }
        }

    }

    private List<String> getEnumValues(Class<?> type)
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


    private List<GraphQLArgument> getGraphQLArguments(DomainQLMethod query)
    {
        List<GraphQLArgument> arguments = new ArrayList<>();
        for (ParameterProvider provider : query.getParameterProviders())
        {
            if (provider instanceof GraphQLValueProvider)
            {
                final GraphQLValueProvider graphQLValueProvider = (GraphQLValueProvider) provider;
                final GraphQLTypeReference typeRef = GraphQLTypeReference.typeRef(graphQLValueProvider.getInputType());
                arguments.add(
                    GraphQLArgument.newArgument()
                        .name(graphQLValueProvider.getArgumentName())
                        .description(graphQLValueProvider.getDescription())
                        .defaultValue(graphQLValueProvider.getDefaultValue())
                        .type(
                            graphQLValueProvider.isRequired() ? GraphQLNonNull.nonNull(typeRef) : typeRef
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

        final Set<Class<?>> jooqInputTypes = new LinkedHashSet<>();

        // define types for the JOOQ Tables
        jooqTables.forEach(table -> defineTypeForTable(builder, table, jooqTables, jooqInputTypes));

        final BiMap<Class<?>, String> map = HashBiMap.create(jooqInputTypes.size() * 3 / 2);

        jooqInputTypes.forEach( cls -> map.put(cls, cls.getSimpleName() + "Input"));
        inputTypes.forEach( cls -> {

            final String name = cls.getSimpleName();

            map.entrySet().removeIf(e -> e.getValue().equals(name));
            map.put(cls, name);

        });

        registerLogic(builder, logicBeans, map);

        defineInputTypes(builder, map);

        builder.additionalDirectives(
            additionalDirectives
        );
    }


    /**
     * Builds all input types
     *
     * @param builder       GraphQL schema builder
     * @param typeToName    maps pojo types to their input type name
     */
    private void defineInputTypes(GraphQLSchema.Builder builder, Map<Class<?>, String> typeToName)
    {
        for (Map.Entry<Class<?>, String> e : typeToName.entrySet())
        {
            final Class<?> type = ensurePojoType(e.getKey());
            final String name = e.getValue();

            log.debug("INPUT TYPE {} {}", name, type.getSimpleName());

            final JSONClassInfo classInfo = JSONUtil.getClassInfo(type);


            final GraphQLInputObjectType.Builder inputBuilder = GraphQLInputObjectType.newInputObject()
                .name(name)
                .description("Generated for " + type.getName());


            for (JSONPropertyInfo info : classInfo.getPropertyInfos())
            {
                final GraphQLField inputFieldAnno = JSONUtil.findAnnotation(info, GraphQLField.class);
                final Class<Object> propertyType = info.getType();
                GraphQLInputType inputType = getGraphQLScalarFor(propertyType, inputFieldAnno);
                if (inputType == null)
                {
                    if (List.class.isAssignableFrom(propertyType))
                    {
                        inputType = (GraphQLInputType) getListType(type, info, false);
                    }
                    else
                    {
                        final String inputTypeName = typeToName.get(propertyType);

                        inputType = inputTypeName != null ? typeRef(inputTypeName) : inputTypeRef(propertyType);
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
                        ": Required field disagreement between @NotNull and @GraphQLField required value");
                }

                final boolean isRequired = (inputFieldAnno != null && inputFieldAnno.required()) || jpaRequired;

                inputBuilder.field(
                    GraphQLInputObjectField.newInputObjectField()
                        .name(inputFieldAnno != null && inputFieldAnno.value().length() > 0 ? inputFieldAnno.value() : info.getJsonName())
                        .type(isRequired ? nonNull(inputType) : inputType)
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
        log.info("typeRef {}", name);

        return new GraphQLTypeReference(name);
    }


    private GraphQLInputType inputTypeRef(Class<?> propertyType)
    {
        if (propertyType.equals(Object.class))
        {
            throw new IllegalStateException();
        }

        final GraphQLScalarType type = DomainQL.getGraphQLScalarFor(propertyType, (GraphQLField) null);
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


        final GraphQLScalarType type = DomainQL.getGraphQLScalarFor(propertyType, null);
        if (type != null)
        {
            return type;
        }
        else
        {
            final String name = propertyType.getSimpleName();
            log.info("outputTypeRef: {}", name);

            return GraphQLTypeReference.typeRef(name);
        }
    }


    private GraphQLType getListType(Class<?> type, JSONPropertyInfo info, boolean isOutputType)
    {
        GraphQLType inputType;
        final Method getterMethod = ((JavaObjectPropertyInfo) info).getGetterMethod();
        final String propertyName = info.getJavaPropertyName();

        return getListType(type, getterMethod, propertyName, isOutputType);
    }


    private GraphQLType getListType(Class<?> type, Method getterMethod, String propertyName, boolean isOutputType)
    {
        GraphQLType inputType;
        final Type genericReturnType = getterMethod.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType))
        {
            throw new DomainQLException(type.getName() + "." + propertyName +
                ": Property getter type must be parametrized.");
        }

        final Class<?> elementClass = (Class<?>) ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];

        inputType = isOutputType ? new GraphQLList(outputTypeRef(elementClass)) : new GraphQLList(inputTypeRef(elementClass));
        return inputType;
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
            final Class<?> pojoType = ensurePojoType(findPojoTypeOf(table));

            final javax.persistence.Table tableAnno = pojoType.getAnnotation(javax.persistence.Table.class);
            final GraphQLObjectType.Builder domainTypeBuilder = GraphQLObjectType.newObject()
                .name(typeName)
                .description(tableAnno != null ? "Generated from " + tableAnno.schema() + "." + table.getName() : null);
            log.debug("DECLARE TYPE {}", typeName);

            final JSONClassInfo classInfo = JSONUtil.getClassInfo(pojoType);


            final Set<String> foreignKeyFields = findForeignKeyFields(table);

            buildFields(domainTypeBuilder, pojoType, foreignKeyFields, null);

            buildForeignKeyFields(domainTypeBuilder, pojoType, classInfo, table);

            buildBackReferenceFields(domainTypeBuilder, pojoType, table, tables);

            final GraphQLObjectType newObjectType = domainTypeBuilder.build();
            builder.additionalType(newObjectType);

            registeredOutputTypes.put(pojoType, newObjectType);

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

            final boolean isRequired = JSONUtil.findAnnotation(fkPropertyInfo, NotNull.class) != null;

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
                final GraphQLType graphQLType = getOutputType(foreignKeyField.getType());
                if (graphQLType == null)
                {
                    throw new IllegalStateException(
                        "Could not determine graphql type for " + foreignKeyField.getType());
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
            }
            else if (sourceField == SourceField.NONE)
            {// nothing to do

            }
        }
    }


    /**
     * Builds the normal fields for the given type.
     * @param domainTypeBuilder     object builder
     * @param classInfo             JSON classInfo for that type
     * @param foreignKeyFields      Names of fields that are part of a foreign keys
     * @param extraOutputTypes      Set to collect further extra output types coming from the fields
     *
     */
    private void buildFields(
        GraphQLObjectType.Builder domainTypeBuilder,
        Class<?> outputType,
        Set<String> foreignKeyFields,
        Set<Class<?>> extraOutputTypes
    )
    {
        final JSONClassInfo classInfo = JSONUtil.getClassInfo(outputType);

        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {
            final Class<Object> type = info.getType();
            final Column jpaColumnAnno = JSONUtil.findAnnotation(info, Column.class);
            final GraphQLField fieldAnno = JSONUtil.findAnnotation(info, GraphQLField.class);
            final GraphQLFetcher fetcherAnno = JSONUtil.findAnnotation(info, GraphQLFetcher.class);

            if (options.isUseDatabaseFieldNames() && jpaColumnAnno == null)
            {
                throw new DomainQLException(type.getSimpleName() + "." + info.getJavaPropertyName() + ": Missing @Column annotation");
            }

            final boolean isRequired = JSONUtil.findAnnotation(info, NotNull.class) != null;

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
            if (extraOutputTypes != null &&  List.class.isAssignableFrom(type))
            {
                graphQLType = getListType(type, info, true);
            }
            else
            {
                final GraphQLScalarType scalarType = DomainQL.getGraphQLScalarFor(type, fieldAnno);
                if (scalarType != null)
                {
                    graphQLType = scalarType;
                }
                else
                {
                    graphQLType = outputTypeRef(type);
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
                .type(isRequired ? GraphQLNonNull.nonNull(graphQLType) : (GraphQLOutputType) graphQLType)
                .dataFetcher(fetcherAnno == null ? new SvensonFetcher(jsonName) : createFetcher(fetcherAnno.value(), fetcherAnno.data(), jsonName))
                .build();

            log.debug("-- {}: {}", fieldDef.getName(), fieldDef.getType().getName());

            domainTypeBuilder.field(
                fieldDef
            );
        }

        MethodAccess methodAccess = null;


        for (Method m : outputType.getMethods())
        {
            Class<?>[] parameterTypes = m.getParameterTypes();
            final GraphQLField fieldAnno = m.getAnnotation(GraphQLField.class);
            if (fieldAnno != null && parameterTypes.length > 0)
            {
                if (methodAccess == null)
                {
                    methodAccess = MethodAccess.get(outputType);
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

                final boolean isRequired = m.getAnnotation(NotNull.class) != null;

                final Class<?> returnType = m.getReturnType();

                GraphQLType graphQLType;
                if (extraOutputTypes != null &&  List.class.isAssignableFrom(returnType))
                {
                    graphQLType = getListType((Class<?>) returnType, m, propertyName, true);
                }
                else
                {
                    final GraphQLScalarType scalarType = DomainQL.getGraphQLScalarFor(returnType, fieldAnno);
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
                            outputType.getSimpleName() + "." + m.getName() + "(" + paramDesc.toString() + ")"
                    )
                    .type(isRequired ? GraphQLNonNull.nonNull(graphQLType) : (GraphQLOutputType) graphQLType)
                    .dataFetcher(new MethodFetcher(methodAccess, methodIndex, parameterNames, parameterTypes));

                for (Parameter parameter : m.getParameters())
                {
                    final Class<?> parameterType = parameter.getType();
                    final GraphQLField paramFieldAnno = parameter.getAnnotation(GraphQLField.class);

                    GraphQLInputType paramGQLType;
                    if (extraOutputTypes != null &&  List.class.isAssignableFrom(parameterType))
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
                        final GraphQLScalarType scalarType = DomainQL.getGraphQLScalarFor(parameterType, paramFieldAnno);
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
                            paramFieldAnno != null && paramFieldAnno.required() ? GraphQLNonNull
                                .nonNull(paramGQLType) : paramGQLType
                        )
                        ;

                    log.info("Method Argument {}: {}", parameter.getName(), paramGQLType);

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

        final String className = cls.getName();
        try
        {
            final Constructor<?> constructor = cls.getConstructor(String.class, String.class);
            if (!DataFetcher.class.isAssignableFrom(cls))
            {
                throw new DomainQLException(cls + " does not implement" + DataFetcher.class.getName());
            }
            return (DataFetcher<?>)constructor.newInstance(jsonName, data);
        }
        catch (NoSuchMethodException e)
        {
            throw new DomainQLException("Cannot create instance of " + cls, e);
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

}
