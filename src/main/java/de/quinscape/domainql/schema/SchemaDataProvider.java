package de.quinscape.domainql.schema;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.util.JSONHolder;
import de.quinscape.spring.jsview.JsViewContext;
import de.quinscape.spring.jsview.JsViewProvider;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

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

    private final JSONHolder schemaData;

    private final String viewDataName;

    // language=GraphQL
    private final static String INTROSPECTION_QUERY =
        "query InputTypesQuery\n" +
            "{\n" +
            "    __schema {\n" +
            "        types {\n" +
            "            ...FullType\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "fragment FullType on __Type {\n" +
            "    kind\n" +
            "    name\n" +
            "    description\n" +
            "    fields(includeDeprecated: true) {\n" +
            "        name\n" +
            "        description\n" +
            "        args {\n" +
            "            ...InputValue\n" +
            "        }\n" +
            "        type {\n" +
            "            ...TypeRef\n" +
            "        }\n" +
            "        isDeprecated\n" +
            "        deprecationReason\n" +
            "    }\n" +
            "    inputFields {\n" +
            "        ...InputValue\n" +
            "    }\n" +
            "    interfaces {\n" +
            "        ...TypeRef\n" +
            "    }\n" +
            "    enumValues(includeDeprecated: true) {\n" +
            "        name\n" +
            "        description\n" +
            "        isDeprecated\n" +
            "        deprecationReason\n" +
            "    }\n" +
            "                possibleTypes {\n" +
            "                    ...TypeRef\n" +
            "                }\n" +
            "            }\n" +
            "\n" +
            "            fragment InputValue on __InputValue {\n" +
            "                name\n" +
            "                description\n" +
            "                type { ...TypeRef }\n" +
            "                defaultValue\n" +
            "            }\n" +
            "\n" +
            "            fragment TypeRef on __Type {\n" +
            "                kind\n" +
            "                name\n" +
            "                ofType {\n" +
            "                    kind\n" +
            "                    name\n" +
            "                    ofType {\n" +
            "                        kind\n" +
            "                        name\n" +
            "                        ofType {\n" +
            "                            kind\n" +
            "                            name\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        ";


    /**
     * Creates a new SchemaDataProvider that uses "schema" as view data name and adds generic type references.
     *
     * @param domainQL DomainQL instance
     */
    public SchemaDataProvider(DomainQL domainQL)
    {
        this(domainQL, DEFAULT_VIEW_DATA_NAME, true);
    }


    /**
     * Creates a new SchemaDataProvider that uses the given view data name that adds generic type references.
     *
     * @param domainQL          DomainQL instance
     * @param viewDataName      name this provider will provide the schema data under
     */
    public SchemaDataProvider(DomainQL domainQL, String viewDataName)
    {
        this(domainQL, viewDataName, true);
    }


    /**
     * Creates a new SchemaDataProvider that uses the given view data name.
     *
     * @param domainQL              DomainQL instance
     * @param viewDataName          name this provider will provide the schema data under
     * @param appendGenericTypes    true to add information about generic types to the provided schema data
     */
    public SchemaDataProvider(DomainQL domainQL, String viewDataName, boolean appendGenericTypes)
    {

        final GraphQL graphQL = GraphQL.newGraphQL(domainQL.getGraphQLSchema()).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query(INTROSPECTION_QUERY)
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);
        Map<String, Object> result = executionResult.getData();

        final Map<String, Object> schemaRoot = (Map<String, Object>) result.get("__schema");
        if (appendGenericTypes)
        {
            schemaRoot.put(GENERIC_TYPES, domainQL.getGenericTypes());
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
