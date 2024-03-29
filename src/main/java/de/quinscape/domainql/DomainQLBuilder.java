package de.quinscape.domainql;

import com.google.common.collect.Maps;
import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.config.Options;
import de.quinscape.domainql.config.RelationModel;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.docs.DocsExtractor;
import de.quinscape.domainql.docs.TypeDoc;
import de.quinscape.domainql.meta.ComputedMetadataProvider;
import de.quinscape.domainql.meta.MetadataProvider;
import de.quinscape.domainql.meta.NameFieldProvider;
import de.quinscape.domainql.param.DataFetchingEnvironmentProviderFactory;
import de.quinscape.domainql.param.ParameterProviderFactory;
import de.quinscape.domainql.param.TypeParameterProviderFactory;
import de.quinscape.domainql.scalar.CurrencyScalar;
import de.quinscape.spring.jsview.util.JSONUtil;
import de.quinscape.spring.jsview.util.Util;
import graphql.Directives;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParser;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.tokenize.InputStreamSource;

import jakarta.persistence.Column;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mutable builder / configurator for {@link DomainQL}.
 */
public class DomainQLBuilder
{
    private final static Logger log = LoggerFactory.getLogger(DomainQLBuilder.class);

    /**
     * Standard GraphQL directives. Are registered by default, unless {@link #withoutStandardDirectives()} is called.
     */
    private final static Set<GraphQLDirective> STANDARD_DIRECTIVES;


    static
    {
        Set<GraphQLDirective> map = new LinkedHashSet<>();
        map.add(Directives.IncludeDirective);
        map.add(Directives.SkipDirective);
        STANDARD_DIRECTIVES = map;
    }


    private final static JSONParser typeDocParser;


    static
    {
        typeDocParser = new JSONParser();
        typeDocParser.addTypeHint("[]", TypeDoc.class);
    }


    private final DSLContext dslContext;

    private final OptionsBuilder optionsBuilder = new OptionsBuilder(this);

    private Collection<ParameterProviderFactory> parameterProviderFactories;

    private Set<Object> logicBeans = new LinkedHashSet<>();

    private List<RelationBuilder> relationBuilders = new ArrayList<>();

    private Map<String, TableLookup> jooqTables = new HashMap<>();

    private Set<GraphQLFieldDefinition> additionalQueries = new LinkedHashSet<>();

    private Set<GraphQLFieldDefinition> additionalMutations = new LinkedHashSet<>();

    private Set<Class<?>> additionalInputTypes = new LinkedHashSet<>();

    private Set<GraphQLDirective> additionalDirectives = new LinkedHashSet<>(STANDARD_DIRECTIVES);

    private boolean fullSupported;

    private Map<Class<?>, GraphQLScalarType> additionalScalarTypes = new LinkedHashMap<>();

    private List<TypeDoc> typeDocs = new ArrayList<>();

    private Set<String> relationIds = new HashSet<>();

    private Map<String, List<String>> nameFields = new LinkedHashMap<>();

    private Set<String> nameFieldsByName = new HashSet<>();

    private Set<MetadataProvider> metadataProviders = new HashSet<>();


    DomainQLBuilder(DSLContext dslContext)
    {
        this.dslContext = dslContext;

        parameterProviderFactories = new LinkedHashSet<>();

        // add default factory
        parameterProviderFactories.add(new DataFetchingEnvironmentProviderFactory());
        parameterProviderFactories.add(new TypeParameterProviderFactory());

        additionalScalarTypes.put(Long.TYPE, CurrencyScalar.newScalar());


    }


    public OptionsBuilder options()
    {
        return new OptionsBuilder(this);
    }


    /**
     * Builds the configured DomainQL helper.
     *
     * @return DomainQL helper
     */
    public DomainQL build()
    {
        final Map<String, Field<?>> fieldLookup = createFieldLookup();

        final Options options = optionsBuilder.buildOptions();
        final List<RelationModel> relationModels = relationBuilders.stream()
            .map(b -> b.build(jooqTables, fieldLookup, options, relationIds))
            .collect(Collectors.toList());


        final HashSet<MetadataProvider> effectiveMetadataProviders = new HashSet<>(new HashSet<>(metadataProviders));
        effectiveMetadataProviders.add(new NameFieldProvider(nameFields, nameFieldsByName));
        effectiveMetadataProviders.add(new ComputedMetadataProvider());

        final DomainQL domainQL = new DomainQL(
            dslContext,
            Collections.unmodifiableSet(logicBeans),
            // XXX: we might need to update the table lookup with overloaded types, so we can't make it unmodifiable it here
            jooqTables,
            Collections.unmodifiableCollection(parameterProviderFactories),
            // XXX: we might need to update the relations with overloaded types, so we can't make it unmodifiable it here
            relationModels,
            options,
            Collections.unmodifiableSet(additionalQueries),
            Collections.unmodifiableSet(additionalMutations),
            Collections.unmodifiableSet(additionalDirectives),
            additionalScalarTypes,
            Collections.unmodifiableSet(additionalInputTypes),
            Collections.unmodifiableList(
                DocsExtractor.normalize(typeDocs)
            ),
            fieldLookup,
            Collections.unmodifiableSet(effectiveMetadataProviders),
            fullSupported
        );

        validateNameFields(domainQL.getGraphQLSchema());

        return domainQL;
    }


    private Map<String, Field<?>> createFieldLookup()
    {
        final Map<String, Field<?>> map = new HashMap<>();

        for (TableLookup lookup : jooqTables.values())
        {
            final Class<?> pojoType = lookup.getPojoType();
            final Table<?> table = lookup.getTable();

            final Map<String, Field<?>> fields = createFieldLookupForType(pojoType, table);

            log.debug("createFieldLookup {}: {}", pojoType.getSimpleName(), fields);

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
            if (!DomainQL.isNormalProperty(info))
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

                final String key = fieldLookupKey(pojoType.getSimpleName(), info.getJsonName());
                fieldsForType.put(key, field);
            }
        }
        return fieldsForType;
    }


    static String fieldLookupKey(String domainType, String fieldName)
    {
        return domainType + ":" + fieldName;
    }


    /**
     * Adds the given collection of parameter provider factories to the DomainQL configuration.
     *
     * @param parameterProviderFactories collection of parameter provider factories
     *
     * @return this builder
     */
    public DomainQLBuilder parameterProviderFactories(Collection<ParameterProviderFactory> parameterProviderFactories)
    {
        this.parameterProviderFactories.addAll(parameterProviderFactories);
        return this;
    }


    /**
     * Adds the given parameter provider factory to the DomainQL configuration.
     *
     * @param parameterProviderFactories parameter provider factory
     *
     * @return this builder
     */
    public DomainQLBuilder parameterProvider(ParameterProviderFactory parameterProviderFactories)
    {
        this.parameterProviderFactories.add(parameterProviderFactories);
        return this;
    }


    /**
     * Returns the currently configured relation configuration map.
     *
     * @return this builder
     */
    public List<RelationBuilder> getRelationBuilders()
    {
        return relationBuilders;
    }


    /**
     * Configures the source and target field generation to use for the given JOOQ foreign key.
     *
     * @param fkField     field contained in a foreign key
     * @param sourceField source field configuration
     * @param targetField target field configuration
     *
     * @return this builder
     * <p>
     * This is the old API, which is less powerful but more succinct for simple cases.
     * <p>
     * More complex configuration is provided by {@link #withRelation(RelationBuilder)}
     *
     * @see #withRelation(RelationBuilder)
     */
    public DomainQLBuilder configureRelation(
        TableField<?, ?> fkField, SourceField sourceField, TargetField targetField
    )
    {
        return configureRelation(
            fkField,
            sourceField,
            targetField,
            null,
            null
        );
    }


    /**
     * Adds a new relation based on the given relation configuration.
     *
     * @param relationBuilder
     *
     * @return
     */
    public DomainQLBuilder withRelation(RelationBuilder relationBuilder)
    {
        relationBuilders.add(relationBuilder);
        return this;
    }


    /**
     * Configures the source and target field generation to use for the given JOOQ foreign key and the
     * field names to generate on both sides.
     * <p>
     * If one of the field configurations is NONE, the corresponding name will be ignored.
     * </p>
     *
     * @param fkField             field contained in a foreign key
     * @param sourceField         source field configuration
     * @param targetField         target field configuration
     * @param leftSideObjectName  object name for the left-hand / source side
     * @param rightSideObjectName object name for the right-hand / target side
     *                            <p>
     *                            This is the old API, which is less powerful but more succinct for simple cases.
     *                            <p>
     *                            More complex configuration is provided by {@link #withRelation(RelationBuilder)}
     *
     * @return this builder
     */
    public DomainQLBuilder configureRelation(
        TableField<?, ?> fkField,
        SourceField sourceField,
        TargetField targetField,
        String leftSideObjectName,
        String rightSideObjectName,
        String ... metaTags
    )
    {
        return withRelation(
            new RelationBuilder()
                .withForeignKeyFields(fkField)
                .withSourceField(sourceField)
                .withTargetField(targetField)
                .withLeftSideObjectName(leftSideObjectName)
                .withRightSideObjectName(rightSideObjectName)
                .withMetaTags(metaTags)
        );
    }


    /**
     * Configures the set of @{@link GraphQLLogic} annotated spring beans to check for query and mutation
     * implementations.
     *
     * @param logicBeans collection of beans annotated with @{@link GraphQLLogic}.
     *
     * @return this builder
     */
    public DomainQLBuilder logicBeans(Collection<Object> logicBeans)
    {
        this.logicBeans.addAll(logicBeans);
        return this;
    }


    /**
     * Configures the given @{@link GraphQLLogic} annotated spring beans to check for query and mutation
     * implementations.
     *
     * @param logicBeans beans var args annotated with @{@link GraphQLLogic}.
     *
     * @return this builder
     */
    public DomainQLBuilder logicBeans(Object... logicBeans)
    {
        Collections.addAll(this.logicBeans, logicBeans);
        return this;
    }


    /**
     * Directly build a GraphQL schema from a DomainQL definition.
     */
    public GraphQLSchema buildGraphQLSchema()
    {
        return this.build().getGraphQLSchema();
    }


    /**
     * Adds all tables of the given JOOQ schema to the DomainQL schema.
     *
     * @param schema JOOQ schema
     *
     * @return this builder
     */
    public DomainQLBuilder objectTypes(Schema schema)
    {
        for (Table<?> table : schema.getTables())
        {
            final Class<?> cls = DomainQL.findPojoTypeOf(table);

            jooqTables.put(cls.getSimpleName(), new TableLookup(cls, table));
        }

        return this;
    }


    /**
     * Adds the given tables to the DomainQL schema.
     *
     * @param tables tables varargs
     *
     * @return this builder
     */
    public DomainQLBuilder objectTypes(Table<?>... tables)
    {
        for (Table<?> table : tables)
        {
            final Class<?> cls = DomainQL.findPojoTypeOf(table);
            jooqTables.put(cls.getSimpleName(), new TableLookup(cls, table));
        }
        return this;
    }


    /**
     * Adds a custom SQL type based on a sql table-like name and a POJO class.
     * <p>
     * The POJO must have a {@link jakarta.persistence.Table} annotation defining the table-like and
     * {@link jakarta.persistence.Table}
     * annotations on the properties defining the column names.
     * <p>
     * {@link jakarta.validation.constraints.NotNull} annotations can be used to mark non-null fields.
     *
     * @param cls POJO type.
     *
     * @return this builder
     */
    public DomainQLBuilder objectType(Class<?> cls)
    {
        final jakarta.persistence.Table anno = cls.getAnnotation(jakarta.persistence.Table.class);
        if (anno == null)
        {
            throw new DomainQLTypeException(
                "Custom SQL based type must have a @javax.persistence.Table annotation defining the name of the " +
                    "table-like"
            );
        }

        final String schema = anno.schema();
        final Table<?> table = schema.length() > 0 ?
            DSL.table(DSL.name(schema, anno.name())) :
            DSL.table(DSL.name(anno.name()));

        jooqTables.put(cls.getSimpleName(), new TableLookup(cls, table));
        return this;
    }


    /**
     * Adds additional query fields to DomainQL query type.
     *
     * @param additionalQueries additional queries
     *
     * @return this builder
     */
    public DomainQLBuilder additionalQueries(GraphQLFieldDefinition... additionalQueries)
    {
        Collections.addAll(this.additionalQueries, additionalQueries);

        return this;
    }


    /**
     * Adds additional mutation fields to DomainQL query type.
     *
     * @param additionalMutations additional mutations
     *
     * @return this builder
     */
    public DomainQLBuilder additionalMutations(GraphQLFieldDefinition... additionalMutations)
    {
        Collections.addAll(this.additionalMutations, additionalMutations);

        return this;
    }


    /**
     * Add additional directives for the GraphQL schema.
     *
     * @param additionalDirectives directives
     *
     * @return this builder
     */
    public DomainQLBuilder withDirectives(GraphQLDirective... additionalDirectives)
    {
        Collections.addAll(this.additionalDirectives, additionalDirectives);
        return this;
    }


    /**
     * Add additional directives for the GraphQL schema.
     *
     * @param additionalDirectives directives
     *
     * @return this builder
     */
    public DomainQLBuilder withDirective(GraphQLDirective additionalDirectives)
    {
        this.additionalDirectives.add(additionalDirectives);
        return this;
    }


    /**
     * Removes the registration for the standard directives.
     *
     * @return this builder
     */
    public DomainQLBuilder withoutStandardDirectives()
    {
        additionalDirectives.removeAll(STANDARD_DIRECTIVES);
        return this;
    }


    public boolean isFullSupported()
    {
        return fullSupported;
    }


    /**
     * Configures whether to support the @full directive for this DomainQL service or not.
     *
     * @return this builder
     */

    public DomainQLBuilder withFullDirectiveSupported(boolean fullSupported)
    {
        this.fullSupported = fullSupported;
        return this;
    }


    public Map<Class<?>, GraphQLScalarType> getAdditionalScalarTypes()
    {
        return additionalScalarTypes;
    }


    /**
     * Adds an additional scalar type
     *
     * @param cls           Java type for the scalar
     * @param scalarType    scalar type to register for the Java Type
     *
     * @return this builder
     */
    public <I,O> DomainQLBuilder withAdditionalScalar(Class<?> cls, GraphQLScalarType scalarType)
    {
        this.additionalScalarTypes.put(cls, scalarType);
        return this;
    }


    public Set<Class<?>> getAdditionalInputTypes()
    {
        return additionalInputTypes;
    }


    /**
     * Adds an additional input type.
     * <p>
     * This can be useful to define additional client-side types without actually accepting them anywhere. Or to
     * define all possible generic domain types.
     *
     * @param inputType java type to add an input type for. <code>Input</code> will be added to the end of the simple
     *                 name if the name does not already end in <code>Input</code>.
     *
     * @return this builder
     */
    public DomainQLBuilder withAdditionalInputType(Class<?> inputType)
    {
        this.additionalInputTypes.add(inputType);

        return this;
    }


    /**
     * Adds additional input types.
     * <p>
     * This can be useful to define additional client-side types without actually accepting them anywhere. Or to
     * define all possible generic domain types.
     *
     * @param inputTypes java types to add an input type for. <code>Input</code> will be added to the end of the
     *                   simple name if the name does not already end in <code>Input</code>.
     *
     * @return this builder
     */
    public DomainQLBuilder withAdditionalInputTypes(Class<?>... inputTypes)
    {
        Collections.addAll(this.additionalInputTypes, inputTypes);

        return this;
    }


    /**
     * Read DomainQL type docs from the given input stream to use as source for schema descriptions.
     * <p>
     * Multiple sources will be merged.
     *
     * @param file file to read JSON data from
     *
     * @return this builder
     */
    public DomainQLBuilder withTypeDocsFrom(File file) throws FileNotFoundException
    {
        if (file == null)
        {
            throw new IllegalArgumentException("file can't be null");
        }
        return withTypeDocsFrom(
            new FileInputStream(file)
        );
    }


    /**
     * Use the given typeDocs as documentation source
     * <p>
     * Multiple sources will be merged.
     *
     * @return this builder
     */
    public DomainQLBuilder withTypeDocs(List<TypeDoc> typeDocs)
    {
        this.typeDocs.addAll(typeDocs);
        return this;
    }


    /**
     * Read DomainQL type docs from the given input stream to use as source for schema descriptions.
     * <p>
     * Multiple sources will be merged.
     *
     * @param is input stream
     *
     * @return this builder
     */
    public DomainQLBuilder withTypeDocsFrom(InputStream is)
    {
        if (is == null)
        {
            throw new IllegalArgumentException("is can't be null");
        }

        //noinspection unchecked
        final List<TypeDoc> typeDocs = typeDocParser.parse(
            List.class,
            new InputStreamSource(
                is,
                true
            )
        );
        return withTypeDocs(typeDocs);
    }


    /**
     * Convenience method to define a single name field for a number of types.
     *
     * @param nameField Single representative name field for each type
     * @param pojoTypes varargs of simple POJO domain types
     *
     * @return this builder
     *
     * @see #configureNameFields(Class, String...)
     */
    public DomainQLBuilder configureNameFieldForTypes(String nameField, Class<?>... pojoTypes)
    {
        if (pojoTypes == null || pojoTypes.length < 1)
        {
            throw new IllegalArgumentException("Need at least one POJO type");
        }

        for (Class<?> pojoType : pojoTypes)
        {
            DomainQL.ensurePojoType(pojoType);
            configureNameFields(pojoType, nameField);
        }
        return this;
    }


    /**
     * Convenience method to define one or more name fields to be automatically used as name field if they are
     * present on the a type.
     * <p>
     * The first field of a type that matches one of the fields will be configured as only name field. If you need
     * multiple
     * name fields on a single type, use {@link #configureNameFields(Class, String...)}.
     *
     * @return this builder
     */
    public DomainQLBuilder configureNameField(String... nameFields)
    {
        if (nameFields == null || nameFields.length < 1)
        {
            throw new IllegalArgumentException("Need at least one name field");
        }

        Collections.addAll(this.nameFieldsByName, nameFields);

        return this;
    }


    /**
     * Adds the given meta data provider instances to be used for DomainQL schema metadata creation.
     *
     * @param metadataProviders Varargs of meta data provider instances.
     *
     * @return this builder
     */
    public DomainQLBuilder withMetadataProviders(MetadataProvider... metadataProviders)
    {
        Collections.addAll(this.metadataProviders, metadataProviders);

        return this;
    }


    /**
     * Configures the given name fields to be representative of the given domain type.
     * <p>
     * This method is needed to define name fields on JOOQ generated POJOs.
     *
     * @param pojoClass  pojo type for the domain type
     * @param nameFields name fields.
     *
     * @return this builder
     */
    public DomainQLBuilder configureNameFields(Class<?> pojoClass, String... nameFields)
    {
        if (nameFields == null || nameFields.length < 1)
        {
            throw new IllegalArgumentException("Need at least one name field");
        }

        DomainQL.ensurePojoType(pojoClass);

        this.nameFields.put(pojoClass.getSimpleName(), Arrays.asList(nameFields));
        return this;
    }


    /**
     * Makes sure that all types and fields declared in {@link #nameFields} actually exist
     * @param graphQLSchema
     */
    private void validateNameFields(GraphQLSchema graphQLSchema)
    {
        for (Map.Entry<String, List<String>> e : nameFields.entrySet())
        {
            final String typeName = e.getKey();
            final List<String> fields = e.getValue();

            final GraphQLType type = graphQLSchema.getType(typeName);
            if (!(type instanceof GraphQLObjectType))
            {
                throw new DomainQLTypeException("Could find named type " + typeName);
            }

            for (String path : fields)
            {
                final List<String> parts = Util.split(path, ".");

                final int numberOfParts = parts.size();
                if (numberOfParts > 0)
                {

                    GraphQLObjectType current = (GraphQLObjectType) type;
                    for (int i = 0; i < numberOfParts - 1; i++)
                    {
                        final GraphQLFieldDefinition fieldDef = current.getFieldDefinition(parts.get(
                            i));
                        if (fieldDef == null)
                        {
                            throw new DomainQLTypeException("Could not find name object field '" + path + "' for type" +
                                " " + typeName);
                        }

                        final GraphQLOutputType fieldType = fieldDef.getType();

                        if (GraphQLTypeUtil.unwrapNonNull(fieldType) instanceof GraphQLList)
                        {
                            throw new DomainQLTypeException("The naming field mechanism does not allow following many-to-many relations");
                        }

                        final GraphQLUnmodifiedType newType = GraphQLTypeUtil.unwrapAll(fieldType);
                        if (!(newType instanceof GraphQLObjectType))
                        {
                            throw new DomainQLTypeException("Could not find name object field '" + path + "' for " +
                                "type" +
                                " " + typeName);
                        }

                        current = (GraphQLObjectType) newType;
                    }


                }
                final GraphQLFieldDefinition fieldDef = ((GraphQLObjectType) type).getFieldDefinition(parts.get(
                    numberOfParts - 1));
                if (fieldDef == null || !(GraphQLTypeUtil.unwrapNonNull(fieldDef.getType()) instanceof GraphQLScalarType))
                {
                    throw new DomainQLTypeException("Could not find name scalar field '" + path + "' for type " + typeName);
                }
            }
        }
    }
}

