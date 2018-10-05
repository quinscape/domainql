package de.quinscape.domainql.preload;

import de.quinscape.spring.jsview.JsViewContext;
import de.quinscape.spring.jsview.JsViewProvider;
import de.quinscape.spring.jsview.loader.ResourceHandle;
import de.quinscape.spring.jsview.loader.ResourceLoader;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.GraphQLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.util.JSONBuilder;

import java.util.List;
import java.util.Map;

/**
 * A js view data provider that executes graphql queries for Javascript entry points.
 */
public final class PreloadedGraphQLQueryProvider
    implements JsViewProvider
{
    private final static Logger log = LoggerFactory.getLogger(PreloadedGraphQLQueryProvider.class);

    private static final String PRELOADED_QUERIES_PATH = "js/preloaded-queries.json";

    private final String NO_DATA =
        JSONBuilder.buildObject()
            .property("data", false)
            .output();

    private final GraphQLSchema graphQLSchema;
    private final ResourceHandle<PreloadedQueries> initialQueriesHandle;


    public PreloadedGraphQLQueryProvider(
        GraphQLSchema graphQLSchema,
        ResourceLoader resourceLoader
    )
    {
        this.graphQLSchema = graphQLSchema;
        this.initialQueriesHandle = resourceLoader.getResourceHandle(
            PRELOADED_QUERIES_PATH,
            new PreloadedQueriesConverter()
        );
    }

    @Override
    public void provide(JsViewContext context) throws Exception
    {
        final String entryPoint = context.getJsView().getEntryPoint();

        final List<PreloadedQuery> queries = initialQueriesHandle.getContent().getQueriesForEntryPoint(
            entryPoint);

        if (queries.size() > 0)
        {
            for (PreloadedQuery query : queries)
            {

                final Object r = executeQuery(query.getQuery());
                context.provideViewData(query.getName(), r);
            }
        }
    }

    private Object executeQuery(Map<String, Object> query)
    {
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query((String) query.get("query"))
            .variables((Map<String, Object>) query.get("variables"))
            .build();

        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

        ExecutionResult executionResult = graphQL.execute(executionInput);
        List<GraphQLError> errors = executionResult.getErrors();

        // we throw if errors occur assuming that there should be no errors in the initial queries, that those queries
        // are correct and up-to-date ..
        if (errors.size() > 0)
        {
            final String message =
                "Error executing initial query:\n" +
                "QUERY = " + query + "\n" +
                "ERRORS = " + JSONUtil.DEFAULT_GENERATOR.forValue(errors);
            log.error(message + " {}", errors);
            throw new PreloadedQueryException(message + errors);
        }

        // .. and since we're not having errors, we just return the pure data.
        return executionResult.getData();
    }
}
