package de.quinscape.domainql;


import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchema;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Execution with the test schema against a mock connection
 */
public class DomainQLExecutionTest
{
    private final static Logger log = LoggerFactory.getLogger(DomainQLExecutionTest.class);


    MockDataProvider provider = new TestProvider();
    MockConnection connection = new MockConnection(provider);

    DSLContext dslContext = DSL.using(connection, SQLDialect.POSTGRES);

    final TestLogic logic = new TestLogic(dslContext);

    final GraphQLSchema schema = DomainQL.newDomainQL(dslContext)
        .objectTypes(Public.PUBLIC)
        .logicBeans(Collections.singleton(logic))

        // source variants
        .configureRelation(Keys.SOURCE_ONE__FK_SOURCE_ONE_TARGET_ID, SourceField.NONE, TargetField.NONE)
        .configureRelation(Keys.SOURCE_TWO__FK_SOURCE_TWO_TARGET_ID, SourceField.SCALAR, TargetField.NONE)
        .configureRelation(Keys.SOURCE_THREE__FK_SOURCE_THREE_TARGET_ID, SourceField.OBJECT, TargetField.NONE)

        // target variants
        .configureRelation(Keys.SOURCE_FIVE__FK_SOURCE_FIVE_TARGET_ID, SourceField.NONE, TargetField.ONE)
        .configureRelation(Keys.SOURCE_SIX__FK_SOURCE_SIX_TARGET_ID, SourceField.NONE, TargetField.MANY)

        .additionalQueries(GraphQLFieldDefinition.newFieldDefinition()
            .name("extraQuery")
            .type(Scalars.GraphQLString)
            .argument(
                GraphQLArgument.newArgument()
                    .name("value")
                    .type(Scalars.GraphQLString)
                    .build()
            )
            .dataFetcher( env -> "extra:" + env.getArgument("value"))
            .build()
        )

        .additionalMutations(GraphQLFieldDefinition.newFieldDefinition()
            .name("extraMutation")
            .type(Scalars.GraphQLString)
            .argument(
                GraphQLArgument.newArgument()
                    .name("value")
                    .type(Scalars.GraphQLString)
                    .build()
            )
            .dataFetcher( env -> "mutated:" + env.getArgument("value"))
            .build()
        )

        .buildGraphQLSchema();


    @Test
    public void testQuery()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query("{ queryTruth }")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors().size(), is(0));
        assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"queryTruth\":true}"));
    }
    @Test
    public void testMutation()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        final HashMap<String, Object> variables = new HashMap<>();
        variables.put("value", "xxx");
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query("mutation testMutation($value: String!) { mutateString(value: $value) }")
            .variables(variables)
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);


        assumeNoErrors(executionResult);

        assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"mutateString\":\"<<xxx>>\"}"));
    }

    @Test
    public void testForeignKeyTraversal()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query("{ walkForwardRef { id target { id } } }")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors().size(), is(0));
        assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"walkForwardRef\":[{\"id\":\"id1\",\"target\":{\"id\":\"id2\"}}]}"));
    }

    @Test
    public void testSingleBackReferenceTraversal()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query("{ walkBackOne { id sourceFive { id } } }")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);


        assumeNoErrors(executionResult);
        assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"walkBackOne\":{\"id\":\"target-id\",\"sourceFive\":{\"id\":\"src-id\"}}}"));
    }


    private void assumeNoErrors(ExecutionResult executionResult)
    {
        final List<GraphQLError> errors = executionResult.getErrors();
        if (errors.size() > 0)
        {
            log.info("ERROR {}", errors);
        }

        assertThat(errors.size(), is(0));
        assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"walkBackOne\":{\"id\":\"target-id\",\"sourceFive\":{\"id\":\"src-id\"}}}"));
    }

    @Test
    public void testManyBackReferenceTraversal()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query("{ walkBackMany { id sourceSixes { id } } }")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors().size(), is(0));
        assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"walkBackMany\":[{\"id\":\"target-id\",\"sourceSixes\":[{\"id\":\"source-id\"},{\"id\":\"source-id2\"}]}]}"));
    }


    @Test
    public void testAdditionalQueries()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query("{ extraQuery(value: \"foo\") }")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);
        assumeNoErrors(executionResult);
        assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"extraQuery\":\"extra:foo\"}"));
    }

    @Test
    public void testAdditionalMutations()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        final HashMap<String, Object> variables = new HashMap<>();
        variables.put("value", "xxx");
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query("mutation testMutation($value: String!) { extraMutation(value: $value) }")
            .variables(variables)
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);
        assumeNoErrors(executionResult);
        assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"extraMutation\":\"mutated:xxx\"}"));
    }
}
