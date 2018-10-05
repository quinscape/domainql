package de.quinscape.domainql;


import com.google.common.collect.ImmutableMap;
import de.quinscape.domainql.beans.CustomFetcherLogic;
import de.quinscape.domainql.beans.DegenerifiedContainerLogic;
import de.quinscape.domainql.beans.DegenerifiedInputLogic;
import de.quinscape.domainql.beans.DegenerifyAndRenameLogic;
import de.quinscape.domainql.beans.DegenerifyContainerLogic;
import de.quinscape.domainql.beans.DoubleDegenerificationLogic;
import de.quinscape.domainql.beans.FullDirectiveLogic;
import de.quinscape.domainql.beans.GetterArgLogic;
import de.quinscape.domainql.beans.LogicWithEnums;
import de.quinscape.domainql.beans.LogicWithEnums2;
import de.quinscape.domainql.beans.MyEnum;
import de.quinscape.domainql.beans.TestLogic;
import de.quinscape.domainql.beans.TypeConversionLogic;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.mock.TestProvider;
import de.quinscape.domainql.scalar.GraphQLTimestampScalar;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
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

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quinscape.domainql.testdomain.Tables.*;
import static graphql.schema.GraphQLNonNull.*;
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
        .logicBeans(Arrays.asList(logic, new TypeConversionLogic()))

        // source variants
        .configureRelation(    SOURCE_ONE.TARGET_ID, SourceField.NONE, TargetField.NONE)
        .configureRelation(    SOURCE_TWO.TARGET_ID, SourceField.SCALAR, TargetField.NONE)
        .configureRelation(  SOURCE_THREE.TARGET_ID, SourceField.OBJECT, TargetField.NONE)
        .configureRelation(   SOURCE_FIVE.TARGET_ID, SourceField.NONE, TargetField.ONE)
        .configureRelation(    SOURCE_SIX.TARGET_ID, SourceField.NONE, TargetField.MANY)
        .configureRelation(    SOURCE_SEVEN.TARGET, SourceField.OBJECT, TargetField.NONE)

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


    {
        log.info("QUERY TYPE: {}", schema.getQueryType());
    }

    @Test
    public void testQuery()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
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
            // language=GraphQL
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
            // language=GraphQL
            .query("{ walkForwardRef { id target { id } } }")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        final List<GraphQLError> errors = executionResult.getErrors();

        log.info(errors.toString());

        assertThat(errors.size(), is(0));
        assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"walkForwardRef\":[{\"id\":\"id1\",\"target\":{\"id\":\"id2\"}}]}"));
    }

    @Test
    public void testSingleBackReferenceTraversal()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
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
    }


    @Test
    public void testManyBackReferenceTraversal()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
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
            // language=GraphQL
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
            // language=GraphQL
            .query("mutation testMutation($value: String!) { extraMutation(value: $value) }")
            .variables(variables)
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);
        assumeNoErrors(executionResult);
        assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"extraMutation\":\"mutated:xxx\"}"));
    }

    @Test
    public void testTypeConvertingMutation()
    {

        final GraphQLSchema schema = DomainQL.newDomainQL(dslContext)
            .logicBeans(Collections.singletonList(new TypeConversionLogic()))
            .buildGraphQLSchema();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        final HashMap<String, Object> variables = new HashMap<>();


        variables.put("target", ImmutableMap.of("name", "qwertz", "created", GraphQLTimestampScalar.toISO8601(new Timestamp(3600))));
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("mutation mutateConverted($target: ConversionTargetInput!) { mutateConverted(target: $target) }")
            .variables(variables)
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);
        assumeNoErrors(executionResult);
        assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"mutateConverted\":\"qwertz:1970-01-01 01:00:03.0\"}"));
    }

    @Test
    public void testCustomFetcher()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(dslContext)
            .logicBeans(Collections.singletonList(new CustomFetcherLogic()))
            .buildGraphQLSchema();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();


        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("query customFetcher { beanWithFetcher { value } }")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);
        assumeNoErrors(executionResult);
        assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"beanWithFetcher\":{\"value\":\"test:Value From Logic\"}}"));
    }


    @Test
    public void testGetterArguments()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(dslContext)
            .logicBeans(Collections.singletonList(new GetterArgLogic()))
            .buildGraphQLSchema();

        log.info(((GraphQLObjectType)schema.getType("GetterArgBean")).getFieldDefinition("modifiedValue").toString());

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        {

            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("query getterArgQuery($arg: String)\n" +
                    "{ \n" +
                    "    getterArgBean { \n" +
                    "        modifiedValue(arg: $arg)" +
                    "    } \n" +
                    "}")
                .variables(ImmutableMap.of("arg", "aaa"))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);
            assumeNoErrors(executionResult);
            assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"getterArgBean\":{\"modifiedValue\":\"Value From GetterArgLogic:aaa:12\"}}"));
        }

        {

            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("query getterArgQuery($arg: String, $num: Int)\n" +
                    "{ \n" +
                    "    getterArgBean { \n" +
                    "        modifiedValue(arg: $arg, num: $num)" +
                    "    } \n" +
                    "}")
                .variables(ImmutableMap.of("arg", "bbb", "num", "1111"))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);
            assumeNoErrors(executionResult);
            assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"getterArgBean\":{\"modifiedValue\":\"Value From GetterArgLogic:bbb:1111\"}}"));
        }

        {

            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("query getterArgQuery($arg: String)\n" +
                    "{ \n" +
                    "    getterArgBean { \n" +
                    "        introduced(arg: $arg){ name }" +
                    "    } \n" +
                    "}")
                .variables(ImmutableMap.of("arg", "ccc"))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);
            assumeNoErrors(executionResult);
            assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"getterArgBean\":{\"introduced\":{\"name\":\"ccc\"}}}"));
        }

    }


    @Test
    public void testEnumOperations()
    {
        {
            final GraphQLSchema schema = DomainQL.newDomainQL(dslContext)
                .logicBeans(Arrays.asList(new LogicWithEnums(), new LogicWithEnums2()))
                .buildGraphQLSchema();

            GraphQL graphQL = GraphQL.newGraphQL(schema).build();


            {

                ExecutionInput executionInput = ExecutionInput.newExecutionInput()

                    // language=GraphQL
                    .query("query queryWithEnumArg($myEnum: MyEnum)\n" +
                        "{ \n" +
                        "    queryWithEnumArg(myEnum: $myEnum)\n" +
                        "}")
                    .variables(ImmutableMap.of("myEnum", MyEnum.C))
                    .build();

                ExecutionResult executionResult = graphQL.execute(executionInput);
                assumeNoErrors(executionResult);
                assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"queryWithEnumArg\":\"(C)\"}"));
            }



            {

                ExecutionInput executionInput = ExecutionInput.newExecutionInput()

                    // language=GraphQL
                    .query("\n" +
                        "query queryWithObjectArgWithEnum($bean: BeanWithEnumInput)\n" +
                        "{ \n" +
                        "    queryWithObjectArgWithEnum(beanWithEnum: $bean)\n" +
                        "}")
                    .variables(ImmutableMap.of("bean", ImmutableMap.of("anotherEnum", "X")))
                    .build();

                ExecutionResult executionResult = graphQL.execute(executionInput);
                assumeNoErrors(executionResult);
                assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"queryWithObjectArgWithEnum\":\"BeanWithEnum: anotherEnum = X\"}"));
            }


            {

                ExecutionInput executionInput = ExecutionInput.newExecutionInput()

                    // language=GraphQL
                    .query("\n" +
                        "mutation enumMutation\n" +
                        "{ \n" +
                        "    enumMutation\n" +
                        "}")
                    .build();

                ExecutionResult executionResult = graphQL.execute(executionInput);
                assumeNoErrors(executionResult);
                assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"enumMutation\":\"B\"}"));
            }

            {

                ExecutionInput executionInput = ExecutionInput.newExecutionInput()

                    // language=GraphQL
                    .query("\n" +
                            "mutation objectWithEnumMutation\n" +
                            "{\n" +
                            "    objectWithEnumMutation{\n" +
                            "        anotherEnum\n" +
                            "    }\n" +
                            "}"
                    )
                    .build();

                ExecutionResult executionResult = graphQL.execute(executionInput);
                assumeNoErrors(executionResult);
                assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"objectWithEnumMutation\":{\"anotherEnum\":\"Z\"}}"    ));
            }
        }
    }


    @Test
    public void testFullDirective()
    {
    final GraphQLSchema schema = DomainQL.newDomainQL(dslContext)
        .logicBeans(Collections.singletonList(new FullDirectiveLogic()))
        .withFullDirectiveSupported(true)
        .buildGraphQLSchema();


        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        {

            final DomainQLExecutionContext domainQLExecutionContext = new DomainQLExecutionContext();
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("    query fullQuery \n" +
                    "    {\n" +
                    "        fullQuery @full\n" +
                    "    }")
                .context(domainQLExecutionContext)
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);
            assumeNoErrors(executionResult);
            assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"fullQuery\":true}"));

            assertThat(JSONUtil.DEFAULT_GENERATOR.forValue(domainQLExecutionContext.getResponse()), is("{\"name\":\"Blafusel\",\"num\":12948}"));
        }
    }

    @Test
    public void testMissingFullDirective()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(dslContext)
            .logicBeans(Collections.singletonList(new FullDirectiveLogic()))
            .withFullDirectiveSupported(true)
            .buildGraphQLSchema();


        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        {

            final DomainQLExecutionContext context = new DomainQLExecutionContext();
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("query fullQuery \n" +
                    "{\n" +
                    "    fullQuery\n" +
                    "}")
                .context(context)
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);
            final List<GraphQLError> errors = executionResult.getErrors();
            assertThat(errors.size(), is(1));
            assertThat(errors.get(0).getMessage(), containsString("Query 'fullQuery' is annotated with (full=true) and cannot be queried without @full"));
        }
    }

    @Test()
    public void testFullDirectiveWithMissingContext()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(dslContext)
            .logicBeans(Collections.singletonList(new FullDirectiveLogic()))
            .withFullDirectiveSupported(true)
            .buildGraphQLSchema();


        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("query fullQuery \n" +
                "{\n" +
                "    fullQuery @full\n" +
                "}")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);


        final List<GraphQLError> errors = executionResult.getErrors();
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0).getMessage(), containsString(" A new de.quinscape.domainql.DomainQLExecutionContext instance or subclass must be provided as .context() in the GraphQL endpoint"));
    }


    @Test
    public void testDegenerify()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new DegenerifyAndRenameLogic()))
            .buildGraphQLSchema();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("query getPayload\n" +
                "{\n" +
                "    getPayload{\n" +
                "        rows{\n" +
                "            name\n" +
                "            num\n" +
                "        }\n" +
                "        rowCount\n" +
                "    }\n" +
                "}")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);


        final List<GraphQLError> errors = executionResult.getErrors();
        assertThat(errors.size(), is(0));

        final Map<String,Object> m = (Map<String, Object>) ((Map<String,Object>)executionResult.getData()).get("getPayload");

        assertThat(m.get("rowCount"), is(2));
        final List rows = (List) m.get("rows");
        assertThat(rows.size(), is(2));

        final String json = JSONUtil.DEFAULT_GENERATOR.forValue(m);

        assertThat(json, containsString("\"aaa\""));
        assertThat(json, containsString("\"bbb\""));
        assertThat(json, containsString("555"));
        assertThat(json, containsString("7777"));

    }

    @Test
    public void testDegenerifiedListInput()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new DegenerifiedInputLogic()))
            .buildGraphQLSchema();


        final GraphQLInputObjectType sourceOneInput = (GraphQLInputObjectType) schema.getType("PagedPayloadInput");
        assertThat(sourceOneInput.getField("rowCount").getType(),is(nonNull(Scalars.GraphQLInt)));
        assertThat(sourceOneInput.getField("rows").getType(),is(nonNull(new GraphQLList(schema.getType("PayloadInput")))));


        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        Map<String, Object> payloadData = JSONUtil.DEFAULT_PARSER.parse(Map.class,"{\n" +
            "    \"rows\": [\n" +
            "        {\n" +
            "            \"name\": \"AAA\",\n" +
            "            \"num\": 100\n" +
            "        }, {\n" +
            "            \"name\": \"BBB\",\n" +
            "            \"num\": 101\n" +
            "        }\n" +
            "    ],\n" +
            "    \"rowCount\": 3\n" +
            "}");
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("mutation mutationWithDegenerifiedInput($pagedPayload: PagedPayloadInput!)\n" +
                "{\n" +
                "    mutationWithDegenerifiedInput(pagedPayload: $pagedPayload)\n" +
                "}")
            .variables(ImmutableMap.of("pagedPayload", payloadData))
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        final List<GraphQLError> errors = executionResult.getErrors();

        log.info(errors.toString());

        assertThat(errors.size(), is(0));
        final Map<String,Object> data = executionResult.getData();

        assertThat(data.get("mutationWithDegenerifiedInput"), is("AAA:100|BBB:101|"));

    }

    @Test
    public void testDegenerifiedInput()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new DegenerifiedContainerLogic()))
            .buildGraphQLSchema();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        Map<String, Object> containerData = JSONUtil.DEFAULT_PARSER.parse(Map.class,"{\n" +
            "    \"value\": {\n" +
            "        \"name\": \"CCC\",\n" +
            "        \"num\": 102\n" +
            "    },\n" +
            "    \"num\": 333\n" +
            "}");
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("query containerQuery($container: ContainerPayloadInput!)\n" +
                "{\n" +
                "    containerQuery(container: $container)\n" +
                "}")
            .variables(ImmutableMap.of("container", containerData))
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        final List<GraphQLError> errors = executionResult.getErrors();

        log.info(errors.toString());

        assertThat(errors.size(), is(0));
        final Map<String,Object> data = executionResult.getData();

        assertThat(data.get("containerQuery"), is("CCC:102:333"));

    }

    @Test
    public void testDegenerifiedContainerOutput()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new DegenerifyContainerLogic()))
            .buildGraphQLSchema();


        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("query queryContainer\n" +
                "{\n" +
                "    queryContainer{\n" +
                "        value{\n" +
                "            name\n" +
                "            num\n" +
                "        }\n" +
                "        num\n" +
                "    }\n" +
                "}")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);


        final List<GraphQLError> errors = executionResult.getErrors();
        log.info(errors.toString());
        assertThat(errors.size(), is(0));

        final Map<String,Object> m = (Map<String, Object>) ((Map<String,Object>)executionResult.getData()).get("queryContainer");

        assertThat(m.get("num"), is(555));
        Map<String,Object> payload = (Map<String, Object>) m.get("value");

        assertThat(payload.get("name"), is("DDD"));
        assertThat(payload.get("num"), is(444));

    }


    @Test
    public void testDoubleDegenerification()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new DoubleDegenerificationLogic()))
            .buildGraphQLSchema();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        Map<String, Object> containerPropData = JSONUtil.DEFAULT_PARSER.parse(Map.class,"{\n" +
            "    \"value\" : {\n" +
            "        \"value\": {\n" +
            "            \"name\": \"EEE\",\n" +
            "            \"num\": 666\n" +
            "        },\n" +
            "        \"num\": 777\n" +
            "    }\n" +
            "}");
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("mutation mutationWithDD($c: ContainerPropPayloadInput!)\n" +
                "{\n" +
                "    mutationWithDD(c: $c)\n" +
                "}")
            .variables(ImmutableMap.of("c", containerPropData))
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);


        final List<GraphQLError> errors = executionResult.getErrors();
        log.info(errors.toString());
        assertThat(errors.size(), is(0));

        final String result = (String) ((Map<String, Object>) executionResult.getData()).get("mutationWithDD");

        assertThat(result, is("[EEE:666:777]"));

    }

}
