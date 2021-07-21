package de.quinscape.domainql.schema;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.util.IntrospectionUtil;
import de.quinscape.domainql.util.JSONHolder;
import de.quinscape.spring.jsview.JsViewContext;
import de.quinscape.spring.jsview.JsViewProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Js view data provider that provides the current graphql input types
 */
public final class SchemaDataProvider
    implements JsViewProvider
{
    /**
     * Property under which the generic type references are provided.
     */
    public static final String DOMAIN_QL = "domainQL";

    public static final String SCHEMA = "schema";
    public static final String META = "meta";

    private final JSONHolder schemaData;

    private final String viewDataName;



    /**
     * Creates a new SchemaDataProvider that uses "schema" as view data name and adds generic type references.
     *
     * @param domainQL DomainQL instance
     */
    public SchemaDataProvider(DomainQL domainQL)
    {
        this(domainQL, DOMAIN_QL, true, true, true);
    }


    /**
     * Creates a new SchemaDataProvider that uses the given view data name that adds generic type references.
     *
     * @param domainQL          DomainQL instance
     * @param viewDataName      name this provider will provide the schema data under
     */
    public SchemaDataProvider(DomainQL domainQL, String viewDataName)
    {
        this(domainQL, viewDataName, true, true,true);
    }


    /**
     * Creates a new SchemaDataProvider that uses the given view data name.
     *
     * @param domainQL              DomainQL instance
     * @param viewDataName          name this provider will provide the schema data under
     * @param appendGenericTypes    true to add information about generic types to the provided schema data
     */
    public SchemaDataProvider(DomainQL domainQL, String viewDataName, boolean appendGenericTypes, boolean appendRelations, boolean appendNameFields)
    {

        final Map<String, Object> data = IntrospectionUtil.introspect(domainQL.getGraphQLSchema());
        final Map<String, Object> schema = (Map<String, Object>) data.get("data");
        final Map<String, Object> schemaRoot = (Map<String, Object>) schema.get("__schema");


        final Map<String, Object> root = new HashMap<>();
        root.put(SCHEMA, schemaRoot);
        root.put(META, domainQL.getMetaData());

        this.schemaData = new JSONHolder(root);
        this.viewDataName = viewDataName;
    }


    @Override
    public void provide(JsViewContext context)
    {
        context.provideViewData(viewDataName, schemaData);
    }
}
