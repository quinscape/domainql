package de.quinscape.domainql;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.config.RelationConfiguration;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.param.DataFetchingEnvironmentProviderFactory;
import de.quinscape.domainql.param.ParameterProviderFactory;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchema;
import org.jooq.DSLContext;
import org.jooq.ForeignKey;
import org.jooq.Schema;
import org.jooq.Table;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mutable builder / configurator for {@link DomainQL}.
 */
public class DomainQLBuilder
{
    private final DSLContext dslContext;

    private final OptionsBuilder optionsBuilder = new OptionsBuilder(this);

    private Collection<ParameterProviderFactory> parameterProviderFactories;

    private Set<Object> logicBeans = new LinkedHashSet<>();

    private Map<ForeignKey<?, ?>, RelationConfiguration> relationConfigurations = new HashMap<>();

    private RelationConfiguration defaultRelationConfiguration = new RelationConfiguration(
        SourceField.NONE, TargetField.NONE);

    private boolean mirrorInputs;

    private Set<Class<?>> inputTypes = new LinkedHashSet<>();

    private Set<Table<?>> jooqTables = new LinkedHashSet<>();

    private Set<GraphQLFieldDefinition> additionalQueries  = new LinkedHashSet<>();
    private Set<GraphQLFieldDefinition> additionalMutations = new LinkedHashSet<>();


    DomainQLBuilder(DSLContext dslContext)
    {
        this.dslContext = dslContext;

        parameterProviderFactories = new LinkedHashSet<>();

        // add default factory
        parameterProviderFactories.add(new DataFetchingEnvironmentProviderFactory());
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
            Collections.unmodifiableSet(inputTypes),
            Collections.unmodifiableCollection(parameterProviderFactories),
            Collections.unmodifiableMap(relationConfigurations),
            defaultRelationConfiguration,
            optionsBuilder.buildOptions(),
            mirrorInputs,
            Collections.unmodifiableSet(additionalQueries),
            Collections.unmodifiableSet(additionalMutations)
        );
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
    public Map<ForeignKey<?, ?>, RelationConfiguration> getRelationConfigurations()
    {
        return relationConfigurations;
    }


    /**
     * Configures the source and target field generation to use for the given JOOQ foreign key.
     *
     * @param foreignKey        foreign key
     * @param sourceField       source field configuration
     * @param targetField       target field configuration
     *
     * @return this builder
     */
    public DomainQLBuilder configureRelation(
        ForeignKey<?, ?> foreignKey, SourceField sourceField, TargetField targetField
    )
    {
        relationConfigurations.put(foreignKey, new RelationConfiguration(sourceField, targetField));
        return this;
    }


    /**
     * Configures the source and target field generation to use for the given JOOQ foreign key and the
     * field names to generate on both sides.
     * <p>
     *     If one of the field configurations is NONE, the corresponding name will be ignored.
     * </p>
     *
     * @param foreignKey        foreign key
     * @param sourceField       source field configuration
     * @param targetField       target field configuration
     * @param leftSideName      field name for the left-hand / source side
     * @param rightSideName     field name for the right-hand / target side
     *
     *
     * @return this builder
     */
    public DomainQLBuilder configureRelation(
        ForeignKey<?, ?> foreignKey,
        SourceField sourceField,
        TargetField targetField,
        String leftSideName,
        String rightSideName
    )
    {
        relationConfigurations.put(
            foreignKey,
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
        final DomainQL domainQL = this.build();
        final GraphQLSchema.Builder builder = GraphQLSchema.newSchema();
        domainQL.register(builder);
        return builder.build();
    }


    /**
     * If set to <code>true</code>, generate a mirror input types for the JOOQ types. For every domain type Foo there
     * will be an identical input type FooInput.
     * <p>
     *     You can still redefine individual input types with {@link #overrideInputTypes(Class[])}
     * </p>
     *
     * @param mirrorInputs  <code>true</code> to create mirror input types
     *
     * @return this builder
     */
    public DomainQLBuilder createMirrorInputTypes(boolean mirrorInputs)
    {
        this.mirrorInputs = mirrorInputs;

        return this;
    }


    /**
     * Registers additional POJOs to create Graphql input types for. Note that input types from Queries and Mutations
     * are automatically registered.
     *
     * @param classes       annotated POJO classes.
     * @return this builder
     */
    public DomainQLBuilder overrideInputTypes(Class<?>... classes)
    {
        Collections.addAll(inputTypes, classes);

        return this;
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
}
