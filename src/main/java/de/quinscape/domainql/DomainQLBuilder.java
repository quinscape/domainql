package de.quinscape.domainql;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.config.RelationConfiguration;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.param.DataFetchingEnvironmentProviderFactory;
import de.quinscape.domainql.param.ParameterProviderFactory;
import de.quinscape.domainql.scalar.GraphQLCurrencyScalar;
import graphql.Directives;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import org.jooq.DSLContext;
import org.jooq.ForeignKey;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mutable builder / configurator for {@link DomainQL}.
 */
public class DomainQLBuilder
{
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

    private final DSLContext dslContext;

    private final OptionsBuilder optionsBuilder = new OptionsBuilder(this);

    private Collection<ParameterProviderFactory> parameterProviderFactories;

    private Set<Object> logicBeans = new LinkedHashSet<>();

    private Map<TableField<?, ?>, RelationConfiguration> relationConfigurations = new HashMap<>();

    private RelationConfiguration defaultRelationConfiguration = new RelationConfiguration(
        SourceField.SCALAR, TargetField.NONE);

    private Set<Table<?>> jooqTables = new LinkedHashSet<>();

    private Set<GraphQLFieldDefinition> additionalQueries  = new LinkedHashSet<>();
    private Set<GraphQLFieldDefinition> additionalMutations = new LinkedHashSet<>();

    private Set<Class<?>> additionalInputTypes = new LinkedHashSet<>();

    private Set<GraphQLDirective> additionalDirectives = new LinkedHashSet<>(STANDARD_DIRECTIVES);

    private boolean fullSupported;

    private Map<Class<?>, GraphQLScalarType> additionalScalarTypes = new LinkedHashMap<>();

    DomainQLBuilder(DSLContext dslContext)
    {
        this.dslContext = dslContext;

        parameterProviderFactories = new LinkedHashSet<>();

        // add default factory
        parameterProviderFactories.add(new DataFetchingEnvironmentProviderFactory());

        additionalScalarTypes.put(Long.TYPE, new GraphQLCurrencyScalar());
    }


    public OptionsBuilder options()
    {
        return new OptionsBuilder(this);
    }


    /**
     * Builds the configured DomainQL helper. Use {@link DomainQL#register(GraphQLSchema.Builder)} to register
     * the helper with an external graphql schema builder.
     *
     * @return  DomainQL helper
     */
    public DomainQL build()
    {
        return new DomainQL(
            dslContext,
            Collections.unmodifiableSet(logicBeans),
            Collections.unmodifiableSet(jooqTables),
            Collections.unmodifiableCollection(parameterProviderFactories),
            Collections.unmodifiableMap( resolveFields( relationConfigurations)),
            defaultRelationConfiguration,
            optionsBuilder.buildOptions(),
            Collections.unmodifiableSet(additionalQueries),
            Collections.unmodifiableSet(additionalMutations),
            Collections.unmodifiableSet(additionalDirectives),
            additionalScalarTypes,
            Collections.unmodifiableSet(additionalInputTypes),
            fullSupported
        );
    }




    private Map<ForeignKey<?, ?>, RelationConfiguration> resolveFields(Map<TableField<?, ?>, RelationConfiguration> relationConfigurations)
    {
        Map<ForeignKey<?, ?>, RelationConfiguration> map = new HashMap<>(relationConfigurations.size());

        for (Map.Entry<TableField<?, ?>, RelationConfiguration> e :
            relationConfigurations
                .entrySet())
        {
            final TableField<?, ?> field = e.getKey();
            final RelationConfiguration config = e.getValue();

            final ForeignKey<?, ?> foreignKey = resolveField(field);
            map.put(foreignKey,config);
        }
        return map;
    }


    private ForeignKey<?, ?> resolveField(TableField<?, ?> field)
    {
        final Table<?> tableOfField = field.getTable();
        for (ForeignKey<?, ?> foreignKey : tableOfField.getReferences())
        {
            if (foreignKey.getFields().indexOf(field) >= 0)
            {
                return foreignKey;
            }
        }

        final String javaTableName = tableOfField.getName().toUpperCase();
        final String javaFieldName = javaTableName + "." + field.getName().toUpperCase();


        throw new DomainQLBuilderException("Field " + javaFieldName + " is not part of any foreign key in " + javaTableName);
    }


    /**
     * Adds the given collection of parameter provider factories to the DomainQL configuration.
     *
     * @param parameterProviderFactories    collection of parameter provider factories
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
     * @param parameterProviderFactories    parameter provider factory
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
    public Map<TableField<?, ?>, RelationConfiguration> getRelationConfigurations()
    {
        return relationConfigurations;
    }


    /**
     * Configures the source and target field generation to use for the given JOOQ foreign key.
     *
     * @param fkField           field contained in a foreign key
     * @param sourceField       source field configuration
     * @param targetField       target field configuration
     *
     * @return this builder
     */
    public DomainQLBuilder configureRelation(
        TableField<?, ?> fkField, SourceField sourceField, TargetField targetField
    )
    {
        relationConfigurations.put(fkField, new RelationConfiguration(sourceField, targetField));
        return this;
    }


    /**
     * Configures the source and target field generation to use for the given JOOQ foreign key and the
     * field names to generate on both sides.
     * <p>
     *     If one of the field configurations is NONE, the corresponding name will be ignored.
     * </p>
     *
     * @param fkField           field contained in a foreign key
     * @param sourceField       source field configuration
     * @param targetField       target field configuration
     * @param leftSideName      field name for the left-hand / source side
     * @param rightSideName     field name for the right-hand / target side
     *
     *
     * @return this builder
     */
    public DomainQLBuilder configureRelation(
        TableField<?, ?> fkField,
        SourceField sourceField,
        TargetField targetField,
        String leftSideName,
        String rightSideName
    )
    {
        relationConfigurations.put(
            fkField,
            new RelationConfiguration(
                sourceField,
                targetField,
                sourceField != SourceField.NONE ? leftSideName : null,
                targetField != TargetField.NONE ? rightSideName : null
            )
        );
        return this;
    }


    /**
     * Returns the current default relation configuration.
     * @return default relation configuration
     */
    public RelationConfiguration getDefaultRelationConfiguration()
    {
        return defaultRelationConfiguration;
    }


    /**
     * Configures the source and target field generation to use as default.
     *
     * @param sourceField       source field configuration
     * @param targetField       target field configuration
     *
     * @return this builder
     */
    public DomainQLBuilder defaultRelationConfiguration(
        SourceField sourceField, TargetField targetField
    )
    {
        this.defaultRelationConfiguration = new RelationConfiguration(sourceField, targetField);
        return this;
    }

    /**
     * Configures the set of @{@link GraphQLLogic} annotated spring beans to check for query and mutation implementations.
     *
     * @param logicBeans    collection of beans annotated with @{@link GraphQLLogic}.
     *                      
     * @return this builder
     */
    public DomainQLBuilder logicBeans(Collection<Object> logicBeans)
    {
        this.logicBeans.addAll(logicBeans);
        return this;
    }

    /**
     * Configures the given @{@link GraphQLLogic} annotated spring beans to check for query and mutation implementations.
     *
     * @param logicBeans   beans var args annotated with @{@link GraphQLLogic}.
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
     * <p>
     *     The alternative to this method is to use {@link #build()} to build a DomainQL object and then call {@link DomainQL#register(GraphQLSchema.Builder)} to register
     *     the DomainQL objects on an external builder.
     * </p>
     *
     * @return graphql ql schema
     */
    public GraphQLSchema buildGraphQLSchema()
    {
        return this.build().getGraphQLSchema();
    }


    public DomainQLBuilder objectTypes(Schema schema)
    {
        jooqTables.addAll(schema.getTables());
        return this;
    }

    public DomainQLBuilder objectTypes(Table<?>... tables)
    {
        Collections.addAll(jooqTables, tables);
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
     * @param additionalDirectives     directives
     * @return  this builder
     */
    public DomainQLBuilder withDirectives(GraphQLDirective... additionalDirectives)
    {
        Collections.addAll(this.additionalDirectives, additionalDirectives);
        return this;
    }

    /**
     * Add additional directives for the GraphQL schema.
     *
     * @param additionalDirectives     directives
     * @return  this builder
     */
    public DomainQLBuilder withDirective(GraphQLDirective additionalDirectives)
    {
        this.additionalDirectives.add( additionalDirectives);
        return this;
    }

    /**
     * Removes the registeration for the standard directives.
     *
     * @return  this builder
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
     * @return  this builder
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
     * @param scalarType    scalar type
     *
     * @return this builder
     */
    public DomainQLBuilder withAdditionalScalar( Class<?> cls, GraphQLScalarType scalarType)
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
     *
     * This can be useful to define additional client-side types without actually accepting them anywhere. Or to
     * define all possible generic domain types.
     *
     * @param inputType     java type to add an input type for. <code>Input</code> will be added to the end of the simple name if the name does not already end in <code>Input</code>.
     *
     * @return  this builder
     */
    public DomainQLBuilder withAdditionalInputType(Class<?> inputType)
    {
        this.additionalInputTypes.add(inputType);

        return this;
    }

    /**
     * Adds additional input types.
     *
     * This can be useful to define additional client-side types without actually accepting them anywhere. Or to
     * define all possible generic domain types.
     *
     * @param inputTypes     java types to add an input type for. <code>Input</code> will be added to the end of the simple name if the name does not already end in <code>Input</code>.
     *
     * @return  this builder
     */
    public DomainQLBuilder withAdditionalInputTypes(Class<?>... inputTypes)
    {
        Collections.addAll(this.additionalInputTypes, inputTypes);

        return this;
    }
}

