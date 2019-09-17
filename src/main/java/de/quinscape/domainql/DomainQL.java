package de.quinscape.domainql;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.collect.Maps;
import de.quinscape.domainql.annotation.GraphQLFetcher;
import de.quinscape.domainql.annotation.GraphQLField;
import de.quinscape.domainql.annotation.GraphQLScalar;
import de.quinscape.domainql.config.Options;
import de.quinscape.domainql.config.RelationConfiguration;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.docs.FieldDoc;
import de.quinscape.domainql.docs.ParamDoc;
import de.quinscape.domainql.docs.TypeDoc;
import de.quinscape.domainql.fetcher.BackReferenceFetcher;
import de.quinscape.domainql.fetcher.FieldFetcher;
import de.quinscape.domainql.fetcher.MethodFetcher;
import de.quinscape.domainql.fetcher.ReferenceFetcher;
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
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.schema.GraphQLNonNull.*;

/**
 * Annotation-based convention-over-configuration GraphQL Schema helper.
 */
public class DomainQL
{
    private final static Logger log = LoggerFactory.getLogger(DomainQL.class);

    //private final static Map<String, GraphQLScalarType> NAME_TO_GRAPHQL;

    public static final String INPUT_SUFFIX = "Input";

    private final Collection<ParameterProviderFactory> parameterProviderFactories;

    private final Options options;

    private final DSLContext dslContext;

    private final Set<Object> logicBeans;

    private final Map<String, TableLookup> jooqTables;

    private final Map<String, Field<?>> dbFieldLookup;

    private final Map<ForeignKey<?, ?>, RelationConfiguration> relationConfigurations;

    private final RelationConfiguration defaultRelationConfiguration;

    private final Set<GraphQLFieldDefinition> additionalQueries;

    private final Set<GraphQLFieldDefinition> additionalMutations;

    private final Set<GraphQLDirective> additionalDirectives;

    private final Set<Class<?>> additionalInputTypes;

    private final List<TypeDoc> typeDocs;

    private final boolean fullSupported;

    private final TypeRegistry typeRegistry;

    private final GraphQLSchema graphQLSchema;

    private final List<GenericTypeReference> genericTypes;


    DomainQL(
        DSLContext dslContext,
        Set<Object> logicBeans,
        Map<String, TableLookup> jooqTables,
        Collection<ParameterProviderFactory> parameterProviderFactories,
        Map<ForeignKey<?, ?>, RelationConfiguration> relationConfigurations,
        RelationConfiguration defaultRelationConfiguration,
        Options options,
        Set<GraphQLFieldDefinition> additionalQueries,
        Set<GraphQLFieldDefinition> additionalMutations,
        Set<GraphQLDirective> additionalDirectives,
        Map<Class<?>, GraphQLScalarType> additionalScalarTypes,
        Set<Class<?>> additionalInputTypes,
        List<TypeDoc> typeDocs,
        boolean fullSupported
    )
    {
        this.dslContext = dslContext;
        this.logicBeans = logicBeans;
        this.relationConfigurations = relationConfigurations;
        this.defaultRelationConfiguration = defaultRelationConfiguration;
        this.additionalQueries = additionalQueries;
        this.additionalMutations = additionalMutations;
        this.additionalDirectives = additionalDirectives;
        this.additionalInputTypes = additionalInputTypes;
        this.typeDocs = typeDocs;
        this.fullSupported = fullSupported;
        this.parameterProviderFactories = parameterProviderFactories;
        this.options = options;


        this.typeRegistry = new TypeRegistry(this, additionalScalarTypes);

        this.jooqTables = jooqTables;
        this.dbFieldLookup = createFieldLookup();

        genericTypes = new ArrayList<>();

        graphQLSchema = this.buildGraphQLSchema();
    }


    private Map<String, Field<?>> createFieldLookup()
    {
        final Map<String, Field<?>> map = new HashMap<>();

        for (TableLookup lookup : jooqTables.values())
        {
            final Class<?> pojoType = lookup.getPojoType();
            final Table<?> table = lookup.getTable();

            final Map<String, Field<?>> fields = createFieldLookupForType(pojoType, table);

            log.debug("createFieldLookup {}: {}", pojoType.getSimpleName(), fields );

            map.putAll(fields);
        }

        return Collections.unmodifiableMap(map);
    }


    private Map<String, Field<?>> createFieldLookupForType(Class<?> pojoType, Table<?> table)
    {
        final JSONClassInfo classInfo = JSONUtil.getClassInfo(pojoType);
        final Map<String, Field<?>> fieldsForType = Maps.newHashMapWithExpectedSize(classInfo.getPropertyInfos()
            .size());

        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {
            if (!isNormalProperty(info))
            {
                continue;
            }

            final Column jpaColumnAnno = JSONUtil.findAnnotation(info, Column.class);
            if (jpaColumnAnno != null)
            {
                final String fieldName = jpaColumnAnno.name();

                final Field<?> fieldFromRecord = table.recordType().field(fieldName);
                final Field<?> field;
                if (fieldFromRecord != null)
                {
                    field = fieldFromRecord;
                }
                else
                {
                    field = DSL.field(DSL.name(fieldName), info.getType());
                }


                final String key = pojoType.getSimpleName() + ":" + info.getJsonName();
                fieldsForType.put(key, field);
            }
        }
        return fieldsForType;
    }


    Map<String, Field<?>> getFieldLookup()
    {
        return dbFieldLookup;
    }


    public Field<?> lookupField(String domainType, String property)
    {
        return dbFieldLookup.get(domainType + ":" + property);
    }


    /**
     * Builds a graphql schema instance from the given DomainQL configuration.
     * @return GraphQL schema
     */
    private GraphQLSchema buildGraphQLSchema()
    {
        final GraphQLSchema.Builder builder = GraphQLSchema.newSchema();
        Set<String> typesForJooqDomain = new HashSet<>();
        final LogicBeanAnalyzer analyzer = registerTypes(builder, typesForJooqDomain);

        defineEnumTypes(builder);

        final Map<String, GraphQLInputObjectType> graphQlInputTypes = defineInputTypes(builder);
        final Map<String, GraphQLObjectType> graphQLOutputTypes = defineOutputTypes(
            builder,
            typesForJooqDomain
        );

        defineQueriesAndMutations(
            builder,
            analyzer,
            graphQlInputTypes,
            graphQLOutputTypes
        );
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

        final GraphQLSchema schema = builder.build();

        this.register(schema);

        return schema;
    }


    public Set<GraphQLDirective> getAdditionalDirectives()
    {
        return additionalDirectives;
    }


    public static String getInputTypeName(Class<?> parameterType)
    {
        if (Enum.class.isAssignableFrom(parameterType))
        {
            return parameterType.getSimpleName();
        }

        final String nameFromType = parameterType.getSimpleName();
        return getInputTypeName(nameFromType);
    }


    public static String getInputTypeName(String outputTypeName)
    {
        if (outputTypeName.endsWith(INPUT_SUFFIX))
        {
            return outputTypeName;
        }
        else
        {
            return outputTypeName + INPUT_SUFFIX;
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
            if (isNormalProperty(info) && info.getJavaPropertyName().equals(javaName))
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
     *
     * @return
     */
    static Class<?> ensurePojoType(Class<?> cls)
    {
        if (!isPojoType(cls))
        {
            throw new DomainQLTypeException(cls.getName() + " is not a simple POJO class. Have you referenced the " +
                "wrong class?");
        }

        final GraphQLScalar annotation = cls.getAnnotation(GraphQLScalar.class);
        if (annotation != null)
        {
            throw new DomainQLTypeException( cls.getName() + " must be declared as scalar (See DomainQLBuilder.withAdditionalScalar)");
        }

        return cls;
    }


    static boolean isPojoType(Class<?> cls)
    {
        return !Table.class.isAssignableFrom(cls) && !Record.class.isAssignableFrom(cls);
    }


    private Set<ForeignKey<?, ?>> findBackReferences(Table<?> table, Collection<TableLookup> tables)
    {
        Set<ForeignKey<?, ?>> set = new LinkedHashSet<>();
        for (TableLookup other : tables)
        {
            for (ForeignKey<?, ?> foreignKey : other.getTable().getReferences())
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


    private JSONPropertyInfo findPropertyInfoForField(Class<?> pojoType, TableField<?, ?> tableField)
    {
        final JSONClassInfo classInfo = JSONUtil.getClassInfo(pojoType);

        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {
//            if (info.isReadOnly())
//            {
//                continue;
//            }
            if (!isNormalProperty(info))
            {
                continue;
            }

            final Column jpaColumnAnno = getColumnAnnotation(pojoType, info);

            final String fieldName = tableField.getName();
            if (fieldName.equals(jpaColumnAnno.name()))
            {
                return info;
            }
        }
        throw new DomainQLException("Cannot find property info for field " + tableField.getName() + " in " + pojoType.getName());
    }


    private Map<String, GraphQLObjectType> defineOutputTypes(
        GraphQLSchema.Builder builder,
        Set<String> typesForJooqDomain
    )
    {

        final Collection<OutputType> outputTypes = typeRegistry.getOutputTypes();
        Map<String,GraphQLObjectType> graphQLTypes = Maps.newHashMapWithExpectedSize(outputTypes.size());

        for (OutputType outputType : outputTypes)
        {
            final Class<?> javaType = outputType.getJavaType();
            final String name = outputType.getName();
            if (typesForJooqDomain.contains(name) || outputType.isEnum() || typeRegistry.getGraphQLScalarFor(
                javaType,
                null
            ) != null)
            {
                continue;
            }

            GraphQLObjectType.Builder domainTypeBuilder = GraphQLObjectType.newObject();
            final TypeContext typeContext = outputType.getTypeContext();

            TypeDoc typeDoc = findTypeDoc(name);

            if (typeDoc == null && typeContext.isParametrized())
            {
                typeDoc = degenerify(findTypeDoc(typeContext.getType().getSimpleName()), typeContext);
            }

            final String defaultDescription = "Generated for " + typeContext.describe();
            domainTypeBuilder
                .name(name)
                .description(typeDoc != null ? typeDoc.getDescription() : defaultDescription);

            registerGenericTypeReference(typeContext);

            log.debug("DECLARE LOGIC TYPE {}", name);

            buildFields(domainTypeBuilder, outputType, Collections.emptySet(), typeDoc);

            final GraphQLObjectType newObjectType = domainTypeBuilder.build();
            builder.additionalType(newObjectType);

            graphQLTypes.put(name, newObjectType);

        }

        return graphQLTypes;
    }


    /**
     * Degenerifies the given type docs by replacing references to generic types.
     *
     * @param typeDoc           typeDoc to degenerify, can be null
     * @param typeContext       type context
     *
     * @return typeDoc with replaced references or null
     */
    private TypeDoc degenerify(TypeDoc typeDoc, TypeContext typeContext)
    {
        if (typeDoc == null)
        {
            return null;
        }

        final TypeDoc copy = new TypeDoc(typeDoc.getName());
        copy.setDescription(
            replaceTypeRefs(typeDoc.getDescription(), typeContext)
        );

        List<FieldDoc> fieldDocsCopy = new ArrayList<>(typeDoc.getFieldDocs().size());
        for (FieldDoc fieldDoc : typeDoc.getFieldDocs())
        {
            fieldDocsCopy.add(
                degenerifyField(
                    fieldDoc,
                    typeContext
                )

            );
        }

        copy.setFieldDocs(fieldDocsCopy);
        return copy;
    }


    private FieldDoc degenerifyField(FieldDoc fieldDoc, TypeContext typeContext)
    {
        if (fieldDoc == null)
        {
            return null;
        }

        return new FieldDoc(
            fieldDoc.getName(),
            replaceTypeRefs(fieldDoc.getDescription(), typeContext)
        );
    }

    private final static Pattern TYPE_REF = Pattern.compile("\\[(.*?)\\]");

    private String replaceTypeRefs(String description, TypeContext typeContext)
    {
        final Matcher m = TYPE_REF.matcher(description);
        StringBuffer sb = new StringBuffer();
        while (m.find())
        {
            final Class<?> aClass = typeContext.resolveType(m.group(1));
            m.appendReplacement(sb, aClass != null ? aClass.getSimpleName() : m.group());
        }
        m.appendTail(sb);

        return sb.toString();
    }


    private TypeDoc findTypeDoc(String name)
    {
        for (TypeDoc typeDoc : typeDocs)
        {
            if (typeDoc.getName().equals(name))
            {
                return typeDoc;
            }
        }
        return null;
    }


    private FieldDoc lookupFieldDoc(TypeDoc typeDoc, String fieldName)
    {
        if (typeDoc != null)
        {
            for (FieldDoc fieldDoc : typeDoc.getFieldDocs())
            {
                if (fieldDoc.getName().equals(fieldName))
                {
                    return fieldDoc;
                }
            }
        }
        return null;
    }


    private void registerGenericTypeReference(TypeContext typeContext)
    {
        final GenericTypeReference ref = GenericTypeReference.create(  typeRegistry, typeContext);
        final boolean isParametrized = ref != null;
        if (isParametrized)
        {
            genericTypes.add(ref);
        }
    }

    private void defineQueriesAndMutations(
        GraphQLSchema.Builder builder,
        LogicBeanAnalyzer analyzer,
        Map<String, GraphQLInputObjectType> graphQlInputTypes,
        Map<String, GraphQLObjectType> graphQLOutputTypes
    )
    {

        log.debug("definesLogicTypes: logic beans = {}", logicBeans);
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

        final TypeDoc queryTypeDoc = findTypeDoc(TypeDoc.QUERY_TYPE);

        for (Query query : queries)
        {

            List<GraphQLArgument> arguments = getGraphQLArguments(query, graphQlInputTypes);

            final FieldDoc fieldDoc = lookMethodDoc(queryTypeDoc, query);

            queryTypeBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
                .name(query.getName())
                .description(fieldDoc != null ? fieldDoc.getDescription() : query.getDescription())
                .type(resolveOutputType(query.getResultType(), graphQLOutputTypes))
                .dataFetcher(query)
                .argument(arguments)
                .build());

        }

        final TypeDoc mutationTypeDoc = findTypeDoc(TypeDoc.MUTATION_TYPE);
        for (Mutation mutation : mutations)
        {
            List<GraphQLArgument> arguments = getGraphQLArguments(mutation, graphQlInputTypes);

            final FieldDoc fieldDoc = lookMethodDoc(mutationTypeDoc, mutation);

            mutationTypeBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
                .name(mutation.getName())
                .description(fieldDoc != null ? fieldDoc.getDescription() : mutation.getDescription())
                .type(mutation.getResultType())
                .dataFetcher(mutation)
                .argument(arguments)
                .build());

        }

        additionalQueries.forEach(queryTypeBuilder::field);
        additionalMutations.forEach(mutationTypeBuilder::field);

        builder.query(queryTypeBuilder);
        builder.mutation(mutationTypeBuilder);
    }


    private FieldDoc lookMethodDoc(TypeDoc queryTypeDoc, DomainQLMethod query)
    {
        FieldDoc fieldDoc = lookupFieldDoc(queryTypeDoc, query.getName());

        final TypeContext queryTypeContext = query.getTypeContext();

        if (queryTypeDoc != null && fieldDoc == null)
        {
            fieldDoc = degenerifyField(lookupFieldDoc(queryTypeDoc, query.getGenericMethodName()), queryTypeContext);
        }
        return fieldDoc;
    }


    private GraphQLOutputType resolveOutputType(
        GraphQLOutputType outputType,
        Map<String, GraphQLObjectType> graphQLOutputTypes
    )
    {
        if (outputType instanceof GraphQLTypeReference)
        {
            final String typeName = outputType.getName();
            final GraphQLObjectType resolved = graphQLOutputTypes.get(typeName);
            if (resolved != null)
            {
                log.debug("Substituting ref for {}", resolved);
                return resolved;
            }
        }

        return outputType;
    }


    public static boolean isNormalProperty(JSONPropertyInfo info)
    {
        return !info.isReadOnly() && !Class.class.isAssignableFrom(info.getType()) && ((JavaObjectPropertyInfo) info).getGetterMethod() != null && !info.isIgnore();
    }


    public GraphQLEnumType buildEnumType(Class<?> nextType)
    {
        final String enumName = nextType.getSimpleName();

        final TypeDoc typeDoc = findTypeDoc(enumName);

        final GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum()
            .name(enumName)
            .description(
                typeDoc != null ? typeDoc.getDescription() : null
            );

        for (String value : getEnumValues(nextType))
        {
            final FieldDoc fieldDoc = lookupFieldDoc(typeDoc, value);

            if (fieldDoc != null)
            {
                enumBuilder.value(value, value, fieldDoc.getDescription());
            }
            else
            {
                enumBuilder.value(value);
            }
        }

        return enumBuilder.build();
    }


    static List<String> getEnumValues(Class<?> type)
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


    private List<GraphQLArgument> getGraphQLArguments(
        DomainQLMethod domainQLMethod,
        Map<String, GraphQLInputObjectType> graphQlInputTypes
    )
    {
        List<GraphQLArgument> arguments = new ArrayList<>();
        for (ParameterProvider provider : domainQLMethod.getParameterProviders())
        {
            if (provider instanceof GraphQLValueProvider)
            {
                final GraphQLValueProvider graphQLValueProvider = (GraphQLValueProvider) provider;
                final GraphQLInputType inputType = resolveInputType(graphQLValueProvider.getInputType(), graphQlInputTypes);
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


    private GraphQLInputType resolveInputType(
        GraphQLInputType inputType,
        Map<String, GraphQLInputObjectType> graphQlInputTypes
    )
    {
        if (inputType instanceof GraphQLTypeReference)
        {
            final String typeName = inputType.getName();
            final GraphQLInputObjectType resolved = graphQlInputTypes.get(typeName);
            if (resolved != null)
            {
                log.debug("Substituting ref for {}", resolved);
                return resolved;
            }
        }

        return inputType;
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


    private LogicBeanAnalyzer registerTypes(GraphQLSchema.Builder builder, Set<String> typesForJooqDomain)
    {
        // define types for the JOOQ Tables
        for (TableLookup table : jooqTables.values())
        {
            final Class<?> pojoType = table.getPojoType();
            typeRegistry.register(new TypeContext(null, pojoType));
            defineTypeForTable(builder, table.getTable(), pojoType, typesForJooqDomain);
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
            final GraphQLEnumType graphQLEnumType = this.buildEnumType(enumType);

            log.debug("DECLARE ENUM TYPE -- {}", enumType);

            builder.additionalType(
                graphQLEnumType
            );
        }

    }


    /**
     * Builds all input types
     *
     * @param builder GraphQL schema builder
     */
    private Map<String, GraphQLInputObjectType> defineInputTypes(GraphQLSchema.Builder builder)
    {
        final Collection<InputType> inputTypes = typeRegistry.getInputTypes();
        Map<String,GraphQLInputObjectType> graphQLTypes = Maps.newHashMapWithExpectedSize(inputTypes.size());

        for (InputType inputType : inputTypes)
        {
            final Class<?> javaType = ensurePojoType(inputType.getJavaType());

            final TypeContext typeContext = inputType.getTypeContext();

            final String name = inputType.getName();

            if (javaType.isEnum())
            {
                continue;
            }

            final GraphQLInputObjectType inputObjectType = buildInputType(inputType, javaType, typeContext, name);
            builder.additionalType(inputObjectType);

            graphQLTypes.put(name, inputObjectType);

        }

        return graphQLTypes;
    }


    private GraphQLInputObjectType buildInputType(
        InputType inputType,
        Class<?> javaType,
        TypeContext typeContext,
        String name
    )
    {
        log.debug("INPUT TYPE {} {}", name, javaType.getSimpleName());

        final JSONClassInfo classInfo = JSONUtil.getClassInfo(javaType);

        final String typeName = inputType.getName();

        TypeDoc typeDoc = findTypeDoc(typeName);
        if (typeDoc == null && typeName.endsWith(INPUT_SUFFIX))
        {
            typeDoc = findTypeDoc(typeName.substring(0, typeName.length() - INPUT_SUFFIX.length()));
        }

        final GraphQLInputObjectType.Builder inputBuilder = GraphQLInputObjectType.newInputObject()
            .name(name)
            .description(typeDoc != null ? typeDoc.getDescription() : "Generated for " + javaType.getName());

        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {
            if (!isNormalProperty(info))
            {
                continue;
            }

            final Method getterMethod = ((JavaObjectPropertyInfo) info).getGetterMethod();
            final GraphQLField inputFieldAnno = JSONUtil.findAnnotation(info, GraphQLField.class);
            final Class<?> propertyType = info.getType();
            GraphQLInputType graphQLFieldType = typeRegistry.getGraphQLScalarFor(propertyType, inputFieldAnno);
            final GraphQLInputObjectField inputField = buildInputField(
                inputType,
                javaType,
                typeContext,
                info,
                getterMethod,
                inputFieldAnno,
                propertyType,
                graphQLFieldType,
                typeDoc
            );
            inputBuilder.field(
                inputField
            );
        }
        return inputBuilder.build();
    }


    private GraphQLInputObjectField buildInputField(
        InputType inputType,
        Class<?> javaType,
        TypeContext typeContext,
        JSONPropertyInfo info,
        Method getterMethod,
        GraphQLField inputFieldAnno,
        Class<?> propertyType,
        GraphQLInputType graphQLFieldType,
        TypeDoc typeDoc
    )
    {
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
                final TypeContext fieldCtx = DegenerificationUtil.getType(
                    inputType.getTypeContext(),
                    inputType,
                    getterMethod
                );
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

        final String fieldName = inputFieldAnno != null && inputFieldAnno.value()
            .length() > 0 ? inputFieldAnno.value() : info.getJsonName();

        final FieldDoc fieldDoc = lookupFieldDoc(typeDoc, fieldName);

        final String defaultDescription = inputFieldAnno != null && inputFieldAnno.description()
            .length() > 0 ? inputFieldAnno.description() : null;
        return GraphQLInputObjectField.newInputObjectField()
            .name(fieldName)
            .description(fieldDoc != null ? fieldDoc.getDescription() : defaultDescription)
            .type(isNotNull ? nonNull(graphQLFieldType) : graphQLFieldType)
            .defaultValue(defaultValue)
            .build();
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


    private GraphQLType getListType(TypeContext outputType, Class<?> type, JSONPropertyInfo info, boolean isOutputType)
    {
        final Method getterMethod = ((JavaObjectPropertyInfo) info).getGetterMethod();
        final String propertyName = info.getJavaPropertyName();

        return getListType(outputType, type, getterMethod, propertyName, isOutputType);
    }


    private GraphQLType getListType(
        TypeContext outputType,
        Class<?> type,
        Method getterMethod,
        String propertyName,
        boolean isOutputType
    )
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

        inputType = isOutputType ? new GraphQLList(outputTypeRef(elementClass)) : new GraphQLList(inputTypeRef(
            elementClass));
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

            final String typeName = pojoType.getSimpleName();

            final javax.persistence.Table tableAnno = pojoType.getAnnotation(javax.persistence.Table.class);
            final String defaultDescription = tableAnno != null ?
                "Generated from " + tableAnno.schema() + "." + table.getName() : null;

            final TypeDoc typeDoc = findTypeDoc(typeName);

            final GraphQLObjectType.Builder domainTypeBuilder = GraphQLObjectType.newObject()
                .name(typeName)
                .description(typeDoc != null ? typeDoc.getDescription() : defaultDescription);
            log.debug("DECLARE TYPE {}", typeName);

            final JSONClassInfo classInfo = JSONUtil.getClassInfo(pojoType);


            final Set<String> foreignKeyFields = findForeignKeyFields(table);

            final OutputType outputType = typeRegistry.lookup(pojoType);

            if (outputType == null)
            {
                throw new IllegalStateException("Could not find output type for type " + pojoType.getName());
            }

            buildFields(domainTypeBuilder, outputType, foreignKeyFields, findTypeDoc(outputType.getName()));

            buildForeignKeyFields(domainTypeBuilder, pojoType, classInfo, table);

            buildBackReferenceFields(domainTypeBuilder, pojoType, classInfo, table, jooqTables.values());

            final GraphQLObjectType newObjectType = domainTypeBuilder.build();


            builder.additionalType(newObjectType);

            typesForJooqDomain.add(outputType.getName());
        }
        catch (Exception e)
        {
            throw new DomainQLTypeException("Error creating type for " + table, e);
        }
    }


    /**
     * Build all fields resulting from a foreign key pointing to the current object type.
     *  @param domainTypeBuilder object builder
     * @param pojoType          pojo type to build the object for
     * @param classInfo         svenson class info
     * @param table             corresponding table
     * @param allTables         set of all tables
     */
    private void buildBackReferenceFields(
        GraphQLObjectType.Builder domainTypeBuilder,
        Class<?> pojoType,
        JSONClassInfo classInfo, Table<?> table,
        Collection<TableLookup> allTables
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

            final GraphQLFieldDefinition fieldDef = buildBackReferenceField(
                pojoType,
                classInfo,
                table,
                foreignKey,
                relationConfiguration,
                targetField,
                foreignKeyField
            );

            domainTypeBuilder.field(
                fieldDef
            );
        }
    }


    private GraphQLFieldDefinition buildBackReferenceField(
        Class<?> pojoType,
        JSONClassInfo classInfo, Table<?> table,
        ForeignKey<?, ?> foreignKey,
        RelationConfiguration relationConfiguration,
        TargetField targetField,
        TableField<?, ?> foreignKeyField
    )
    {
        final Table<?> otherTable = foreignKey.getTable();
        final Class<?> otherPojoType = findPojoTypeOf(otherTable);

        final boolean isOneToOne = targetField == TargetField.ONE;

        final GraphQLOutputType type = outputTypeRef(otherPojoType);

        final String backReferenceFieldName;
        if (relationConfiguration.getRightSideObjectName() != null)
        {
            backReferenceFieldName = relationConfiguration.getRightSideObjectName();
        }
        else
        {
            final String otherName = Introspector.decapitalize(otherPojoType.getSimpleName());
            backReferenceFieldName = isOneToOne ? otherName : options.getPluralizationFunction().apply(otherName);
        }
        if (classInfo.getPropertyInfo(backReferenceFieldName) != null)
        {
            throw new DomainQLTypeException("Invalid object field name " + pojoType.getSimpleName() + "."  + backReferenceFieldName + ":  exists both as object field and as scalar field.");
        }

        final JSONPropertyInfo fkPropertyInfo = findPropertyInfoForField(
            pojoType,
            table.getPrimaryKey().getFields().get(0)
        );

        final boolean isNotNull = JSONUtil.findAnnotation(fkPropertyInfo, NotNull.class) != null;

        final GraphQLOutputType fieldType = isOneToOne ? type : new GraphQLList(type);
        final GraphQLFieldDefinition.Builder backReferenceField = GraphQLFieldDefinition.newFieldDefinition()
            .name(backReferenceFieldName)
            .description((isOneToOne ? "One-to-one object" : "Many-to-many objects") + " from '" + otherTable.getName() + "." + foreignKeyField
                .getName() + "'")
            .type(isNotNull ? nonNull(fieldType) : fieldType)
            .dataFetcher(
                new BackReferenceFetcher(
                    dslContext,
                    fkPropertyInfo.getJsonName(),
                    otherTable,
                    otherPojoType,
                    foreignKey,
                    isOneToOne
                )
            );
        final GraphQLFieldDefinition fieldDef = backReferenceField.build();
        log.debug("-- fk {} {}", isOneToOne ? "backref" : "backrefs", fieldDef);
        return fieldDef;
    }


    /**
     * Build the fields resulting from the foreign keys of this type.
     *
     * @param domainTypeBuilder object builder
     * @param pojoType          pojo type to build the object for
     * @param classInfo         JSON classInfo for that type
     * @param table             corresponding table
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


            // we ignore foreign keys that are configured with NONE and keys with more than one field
            final RelationConfiguration relationConfiguration = getRelationConfiguration(foreignKey);
            final SourceField sourceField = relationConfiguration.getSourceField();
            if (fields.size() != 1)
            {
                continue;
            }
            final TableField<?, ?> foreignKeyField = fields.get(0);
            final JSONPropertyInfo fkPropertyInfo = findPropertyInfoForField(
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
                final GraphQLType graphQLType = outputTypeRef(foreignKeyField.getType());
                if (graphQLType == null)
                {
                    throw new IllegalStateException(
                        "Could not determine graphql type for " + foreignKeyField.getType());
                }
                final GraphQLFieldDefinition fieldDef = GraphQLFieldDefinition.newFieldDefinition()
                    .name(javaName)
                    .description("DB foreign key column '" + foreignKeyField.getName() + "'")
                    .type(isNotNull ? GraphQLNonNull.nonNull(graphQLType) : (GraphQLOutputType) graphQLType)
                    .dataFetcher(new FieldFetcher(pojoType.getSimpleName(), findJsonName(classInfo, javaName), foreignKeyField.getType()))
                    .build();
                log.debug("-- fk scalar {}", fieldDef);
                domainTypeBuilder.field(
                    fieldDef
                );
            }

            if (sourceField == SourceField.OBJECT || sourceField == SourceField.OBJECT_AND_SCALAR)
            {
                final String objectFieldName;
                if (relationConfiguration.getLeftSideObjectName() != null)
                {
                    objectFieldName = relationConfiguration.getLeftSideObjectName();
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

                if (classInfo.getPropertyInfo(objectFieldName) != null)
                {
                    throw new DomainQLTypeException("Invalid object field name " + pojoType.getSimpleName() + "."  + objectFieldName + ":  exists both as object field and as scalar field.");
                }

                final TableField<?, ?> targetField = foreignKey.getKey().getFields().get(0);

                final Class<?> otherPojoType = findPojoTypeOf(foreignKey.getKey().getTable());
                final GraphQLOutputType objectRef = outputTypeRef(otherPojoType);
                final GraphQLFieldDefinition fieldDef = GraphQLFieldDefinition.newFieldDefinition()
                    .name(objectFieldName)
                    .description("Target of '" + foreignKeyField.getName() + "'")
                    .type(isNotNull ? GraphQLNonNull.nonNull(objectRef) : objectRef)
                    .dataFetcher(
                        new ReferenceFetcher(
                            dslContext,
                            objectFieldName,
                            findJsonName(classInfo, javaName),
                            foreignKey.getKey().getTable(),
                            findPojoTypeOf(foreignKey.getKey().getTable()),
                            targetField.getName()
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
     *  @param outputType       output type reference
     * @param foreignKeyFields Names of fields that are part of a foreign keys
     * @param typeDoc
     */
    private void buildFields(
        GraphQLObjectType.Builder domainTypeBuilder,
        OutputType outputType,
        Set<String> foreignKeyFields,
        TypeDoc typeDoc
    )
    {
        final Class<?> javaType = outputType.getJavaType();

        handlePropertyFields(domainTypeBuilder, outputType, foreignKeyFields, javaType, typeDoc);
        handleParametrizedFields(domainTypeBuilder, outputType, javaType, typeDoc);
    }


    private void handlePropertyFields(
        GraphQLObjectType.Builder domainTypeBuilder,
        OutputType outputType,
        Set<String> foreignKeyFields,
        Class<?> javaType,
        TypeDoc typeDoc
    )
    {
        final JSONClassInfo classInfo = JSONUtil.getClassInfo(javaType);

        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {

            if (!isNormalProperty(info))
            {
                continue;
            }

            final GraphQLFieldDefinition fieldDef = getPropertyFieldDefinition(javaType.getSimpleName(), outputType, foreignKeyFields, info, typeDoc);
            if (fieldDef == null)
            {
                continue;
            }

            domainTypeBuilder.field(
                fieldDef
            );
        }
    }


    private void handleParametrizedFields(
        GraphQLObjectType.Builder domainTypeBuilder,
        OutputType outputType,
        Class<?> javaType,
        TypeDoc typeDoc
    )
    {
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

                final GraphQLFieldDefinition fieldDef = createParametrizedField(
                    outputType,
                    javaType,
                    methodAccess,
                    m,
                    parameterTypes,
                    fieldAnno,
                    typeDoc
                );

                log.debug("-- {}: {}", fieldDef.getName(), fieldDef.getType().getName());

                domainTypeBuilder.field(
                    fieldDef
                );

            }
        }
    }


    private GraphQLFieldDefinition createParametrizedField(
        OutputType outputType,
        Class<?> javaType,
        MethodAccess methodAccess,
        Method m,
        Class<?>[] parameterTypes,
        GraphQLField fieldAnno,
        TypeDoc typeDoc
    )
    {
        final int methodIndex = methodAccess.getIndex(m.getName(), parameterTypes);

        final String propertyName = getPropertyName(m);

        final boolean isNotNull = m.getAnnotation(NotNull.class) != null;

        final Class<?> returnType = m.getReturnType();

        final GraphQLType graphQLType = getFieldType(outputType, m, fieldAnno, propertyName, returnType);

        final List<String> parameterNames = getParameterNames(m);

        final String fieldName = fieldAnno.value().length() > 0 ? fieldAnno.value() : propertyName;

        final FieldDoc fieldDoc = lookupFieldDoc(typeDoc, fieldName);
        final String description = fieldDoc != null ? fieldDoc.getDescription() : fieldAnno.description();

        final GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
            .name(fieldName)
            .description(description)
            .type(isNotNull ? GraphQLNonNull.nonNull(graphQLType) : (GraphQLOutputType) graphQLType)
            .dataFetcher(new MethodFetcher(methodAccess, methodIndex, parameterNames, parameterTypes));

        for (Parameter parameter : m.getParameters())
        {
            final Class<?> parameterType = parameter.getType();
            final GraphQLField paramFieldAnno = parameter.getAnnotation(GraphQLField.class);

            final GraphQLArgument arg = buildArgument(propertyName, parameter, parameterType, paramFieldAnno, fieldDoc);
            fieldBuilder.argument(arg);
        }

        return fieldBuilder
            .build();
    }


    private List<String> getParameterNames(Method m)
    {
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
        return parameterNames;
    }


    private GraphQLType getFieldType(
        OutputType outputType,
        Method m,
        GraphQLField fieldAnno,
        String propertyName,
        Class<?> returnType
    )
    {
        GraphQLType graphQLType;
        if (List.class.isAssignableFrom(returnType))
        {
            graphQLType = getListType(outputType.getTypeContext(), returnType, m, propertyName, true);
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
        return graphQLType;
    }


    private String describeParameter(Class<?>[] parameterTypes)
    {
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
        return paramDesc.toString();
    }


    private GraphQLArgument buildArgument(
        String propertyName,
        Parameter parameter,
        Class<?> parameterType,
        GraphQLField paramFieldAnno,
        FieldDoc fieldDoc
    )
    {
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

        final String paramName = parameter.getName();
        final String description = findParamDoc(fieldDoc, paramName, "");


        final GraphQLArgument.Builder arg = GraphQLArgument.newArgument()
            .name(paramName)
            .description(description)
            .defaultValue(paramFieldAnno != null ? paramFieldAnno.defaultValue() : null)
            .type(
                paramFieldAnno != null && paramFieldAnno.notNull() ? GraphQLNonNull
                    .nonNull(paramGQLType) : paramGQLType
            );

        log.debug("Method Argument {}: {}", paramName, paramGQLType);
        return arg.build();
    }


    private String findParamDoc(FieldDoc fieldDoc, String paramName, String defaultValue)
    {
        if (fieldDoc != null)
        {
            for (ParamDoc paramDoc : fieldDoc.getParamDocs())
            {
                if (paramDoc.getName().equals(paramName))
                {
                    return paramDoc.getDescription();
                }
            }
        }
        return defaultValue;
    }


    private GraphQLFieldDefinition getPropertyFieldDefinition(
        String domainType,
        OutputType outputType,
        Set<String> foreignKeyFields,
        JSONPropertyInfo info,
        TypeDoc typeDoc
    )
    {
        final Class<Object> type = info.getType();
        final Column jpaColumnAnno = JSONUtil.findAnnotation(info, Column.class);
        final GraphQLField fieldAnno = JSONUtil.findAnnotation(info, GraphQLField.class);
        final GraphQLFetcher fetcherAnno = JSONUtil.findAnnotation(info, GraphQLFetcher.class);

        final Method getterMethod = ((JavaObjectPropertyInfo) info).getGetterMethod();


        if (options.isUseDatabaseFieldNames() && jpaColumnAnno == null)
        {
            throw new DomainQLException(type.getSimpleName() + "." + info.getJavaPropertyName() + ": Missing @Column " +
                "annotation");
        }

        final boolean isNotNull = JSONUtil.findAnnotation(info, NotNull.class) != null;

        if (jpaColumnAnno != null && foreignKeyFields.contains(jpaColumnAnno.name()))
        {
            // ignore foreign key fields
            return null;
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
                graphQLType = new GraphQLTypeReference(DegenerificationUtil.getType(
                    outputType.getTypeContext(),
                    outputType,
                    getterMethod
                ).getTypeName());
            }
        }

        final GraphQLFieldDefinition fieldDef;

        final String defaultDescription = fieldAnno != null && fieldAnno.description().length() > 0 ?
            fieldAnno.description() :
            jpaColumnAnno != null ?
                "DB column '" + jpaColumnAnno.name() + "'" :
                "";

        final String fieldName = fieldAnno != null && fieldAnno.value().length() > 0 ? fieldAnno.value() : name;

        final FieldDoc fieldDoc = lookupFieldDoc(typeDoc, fieldName);

        fieldDef = GraphQLFieldDefinition.newFieldDefinition()
            .name(fieldName)
            .description(
                fieldDoc != null ? fieldDoc.getDescription() : defaultDescription
            )
            .type(isNotNull ? GraphQLNonNull.nonNull(graphQLType) : (GraphQLOutputType) graphQLType)
            .dataFetcher(fetcherAnno == null ? new FieldFetcher(domainType, jsonName, type) : createFetcher(
                fetcherAnno.value(),
                fetcherAnno.data(),
                jsonName
            ))
            .build();

        log.debug("-- {}: {}", fieldDef.getName(), fieldDef.getType().getName());
        return fieldDef;
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
                throw new DomainQLException("Fetcher constructor can take 2 most two parameters");
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
                return (DataFetcher<?>) ctor.newInstance();
            }
            else if (parameterTypes.length == 1)
            {
                return (DataFetcher<?>) ctor.newInstance(jsonName);
            }
            else
            {
                return (DataFetcher<?>) ctor.newInstance(jsonName, data);
            }
        }
        catch (IllegalAccessException e)
        {
            throw new DomainQLException("Cannot access constructor" + className + "(String,String).", e);
        }
        catch (InstantiationException e)
        {
            throw new DomainQLException("Cannot instantiate " + className, e);
        }
        catch (InvocationTargetException e)
        {
            throw new DomainQLException("Error instantiating " + className, e.getTargetException());
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


    public Table<?> getJooqTable(String domainType)
    {
        final TableLookup lookup = jooqTables.get(domainType);
        if (lookup == null)
        {
            throw new DomainQLException("No jooq table registered for domain type '" + domainType + "'");
        }
        return lookup.getTable();
    }


    public Class<?> getPojoType(String domainType)
    {
        return jooqTables.get(domainType).getPojoType();
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


    public List<GenericTypeReference> getGenericTypes()
    {
        return genericTypes;
    }


    void register(GraphQLSchema schema)
    {
        for (GraphQLScalarType scalarType : getTypeRegistry().getScalarTypes())
        {
            if (scalarType instanceof DomainQLAware)
            {
                ((DomainQLAware) scalarType).setDomainQL(this);
            }
        }
    }


    public GraphQLSchema getGraphQLSchema()
    {
        return graphQLSchema;
    }
}


