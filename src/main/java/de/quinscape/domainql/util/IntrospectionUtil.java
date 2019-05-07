package de.quinscape.domainql.util;

import de.quinscape.domainql.DomainQL;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

import java.util.Map;

/**
 * GraphQL schema introspection utility class used by {@link de.quinscape.domainql.schema.SchemaDataProvider}.
 */
public final class IntrospectionUtil
{
    // language=GraphQL
    public final static String INTROSPECTION_QUERY =
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

    private IntrospectionUtil()
    {
        // no instances
    }


    /**
     * Executes the {@link #INTROSPECTION_QUERY} against the given GraphQL schema.
     *
     * @param graphQLSchema     GraphQL schema
     *
     * @return raw introspection data including "__schema" key.
     */
    public static Map<String, Object> introspect(GraphQLSchema graphQLSchema)
    {
        final GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query(INTROSPECTION_QUERY)
            .build();

        return graphQL.execute(executionInput).toSpecification();
    }

}
