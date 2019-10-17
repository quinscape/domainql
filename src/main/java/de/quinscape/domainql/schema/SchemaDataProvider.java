package de.quinscape.domainql.schema;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.util.IntrospectionUtil;
import de.quinscape.domainql.util.JSONHolder;
import de.quinscape.spring.jsview.JsViewContext;
import de.quinscape.spring.jsview.JsViewProvider;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.SchemaUtil;
import graphql.schema.idl.SchemaPrinter;
import org.svenson.util.JSONBuilder;

import java.util.Map;

/**
 * Js view data provider that provides the current graphql input types
 */
public final class SchemaDataProvider
    implements JsViewProvider
{
    private static final String DEFAULT_VIEW_DATA_NAME = "schema";

    /**
     * Property under which the generic type references are provided.
     */
    private static final String GENERIC_TYPES = "genericTypes";

    private static final String RELATIONS = "relations";

    private final JSONHolder schemaData;

    private final String viewDataName;

    /**
     * Creates a new SchemaDataProvider that uses "schema" as view data name and adds generic type references.
     *
     * @param domainQL DomainQL instance
     */
    public SchemaDataProvider(DomainQL domainQL)
    {
        this(domainQL, DEFAULT_VIEW_DATA_NAME, true, true);
    }


    /**
     * Creates a new SchemaDataProvider that uses the given view data name that adds generic type references.
     *
     * @param domainQL          DomainQL instance
     * @param viewDataName      name this provider will provide the schema data under
     */
    public SchemaDataProvider(DomainQL domainQL, String viewDataName)
    {
        this(domainQL, viewDataName, true, true);
    }


    /**
     * Creates a new SchemaDataProvider that uses the given view data name.
     *
     * @param domainQL              DomainQL instance
     * @param viewDataName          name this provider will provide the schema data under
     * @param appendGenericTypes    true to add information about generic types to the provided schema data
     */
    public SchemaDataProvider(DomainQL domainQL, String viewDataName, boolean appendGenericTypes, boolean appendRelations)
    {

        final Map<String, Object> data = IntrospectionUtil.introspect(domainQL.getGraphQLSchema());
        final Map<String, Object> schema = (Map<String, Object>) data.get("data");
        final Map<String, Object> schemaRoot = (Map<String, Object>) schema.get("__schema");
        if (appendGenericTypes)
        {
            schemaRoot.put(GENERIC_TYPES, domainQL.getGenericTypes());
        }

        if (appendRelations)
        {
            schemaRoot.put(RELATIONS, domainQL.getRelationModels());
        }

        this.schemaData = new JSONHolder(schemaRoot);
        this.viewDataName = viewDataName;
    }


    @Override
    public void provide(JsViewContext context)
    {
        context.provideViewData(viewDataName, schemaData);
    }
}
