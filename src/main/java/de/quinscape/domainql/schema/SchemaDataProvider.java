package de.quinscape.domainql.schema;

import de.quinscape.spring.jsview.JsViewContext;
import de.quinscape.spring.jsview.JsViewProvider;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

import java.util.Map;

/**
 * Js view data provider that provides the current graphql input types
 */
public final class SchemaDataProvider
    implements JsViewProvider
{
    private static final String DEFAULT_VIEW_DATA_NAME = "schema";

    private final Object schemaData;

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
     * Creates a new SchemaDataProvider that uses "schema" as view data name.
     * 
     * @param schema            GraphQL schema
     */
    public SchemaDataProvider(GraphQLSchema schema)
    {
        this(schema, DEFAULT_VIEW_DATA_NAME);
    }


    /**
     * Creates a new SchemaDataProvider that uses the given view data name.
     *
     * @param schema            GraphQL schema
     * @param viewDataName      name this provider will provide the schema data under
     */
    public SchemaDataProvider(GraphQLSchema schema, String viewDataName)
    {

        final GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query(INTROSPECTION_QUERY)
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);
        Map<String,Object> result = executionResult.getData();

        this.schemaData = result.get("__schema");
        this.viewDataName = viewDataName;
    }


    @Override
    public void provide(JsViewContext context) throws Exception
    {
        context.provideViewData(viewDataName, schemaData);
    }
}
