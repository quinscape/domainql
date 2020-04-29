package de.quinscape.domainql;


import com.google.common.collect.ImmutableMap;
import de.quinscape.domainql.beans.GenericScalarLogic;
import de.quinscape.domainql.beans.MyEnum;
import de.quinscape.domainql.beans.SumPerMonth;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.generic.DomainObjectScalar;
import de.quinscape.domainql.generic.GenericScalar;
import de.quinscape.domainql.generic.GenericScalarType;
import de.quinscape.domainql.logicimpl.AccessDomainQLLogic;
import de.quinscape.domainql.logicimpl.CustomFetcherLogic;
import de.quinscape.domainql.logicimpl.DegenerifiedContainerLogic;
import de.quinscape.domainql.logicimpl.DegenerifiedInputLogic;
import de.quinscape.domainql.logicimpl.DegenerifyAndRenameLogic;
import de.quinscape.domainql.logicimpl.DegenerifyContainerLogic;
import de.quinscape.domainql.logicimpl.DoubleDegenerificationLogic;
import de.quinscape.domainql.logicimpl.FetcherContextLogic;
import de.quinscape.domainql.logicimpl.FullDirectiveLogic;
import de.quinscape.domainql.logicimpl.GenericDomainLogic;
import de.quinscape.domainql.logicimpl.GenericDomainOutputLogic;
import de.quinscape.domainql.logicimpl.GetterArgLogic;
import de.quinscape.domainql.logicimpl.ListInputLogic;
import de.quinscape.domainql.logicimpl.LogicWithEnums;
import de.quinscape.domainql.logicimpl.LogicWithEnums2;
import de.quinscape.domainql.logicimpl.NullForComplexValueLogic;
import de.quinscape.domainql.logicimpl.SumPerMonthLogic;
import de.quinscape.domainql.logicimpl.TestLogic;
import de.quinscape.domainql.logicimpl.TypeConversionLogic;
import de.quinscape.domainql.logicimpl.TypeParamLogic;
import de.quinscape.domainql.logicimpl.TypeParamMutationLogic;
import de.quinscape.domainql.mock.TestProvider;
import de.quinscape.domainql.scalar.GraphQLTimestampScalar;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.pojos.Foo;
import de.quinscape.domainql.testdomain.tables.pojos.TargetNine;
import de.quinscape.domainql.testdomain.tables.pojos.TargetNineCounts;
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
import org.svenson.util.JSONPathUtil;

import java.sql.Timestamp;
import java.time.Instant;
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


    private MockDataProvider provider = new TestProvider();

    private MockConnection connection = new MockConnection(provider);

    private DSLContext dslContext = DSL.using(connection, SQLDialect.POSTGRES);

    private final TestLogic logic = new TestLogic(dslContext);

    private final GraphQLSchema schema = DomainQL.newDomainQL(dslContext)
        .objectTypes(Public.PUBLIC)
        .logicBeans(Arrays.asList(logic, new TypeConversionLogic()))

        // source variants
        .withRelation(
            new RelationBuilder()
                .withForeignKeyFields(SOURCE_ONE.TARGET_ID)
                .withSourceField(SourceField.NONE)
                .withTargetField(TargetField.NONE)
        )

        .withRelation(
            new RelationBuilder()
                .withForeignKeyFields(SOURCE_TWO.TARGET_ID)
                .withSourceField(SourceField.SCALAR)
                .withTargetField(TargetField.NONE)
        )

        .withRelation(
            new RelationBuilder()
                .withForeignKeyFields(SOURCE_THREE.TARGET_ID)
                .withSourceField(SourceField.OBJECT)
                .withTargetField(TargetField.NONE)
        )

        .withRelation(
            new RelationBuilder()
                .withForeignKeyFields(SOURCE_FIVE.TARGET_ID)
                .withSourceField(SourceField.NONE)
                .withTargetField(TargetField.ONE)
        )

        .withRelation(
            new RelationBuilder()
                .withForeignKeyFields(SOURCE_SIX.TARGET_ID)
                .withSourceField(SourceField.NONE)
                .withTargetField(TargetField.MANY)
        )

        .withRelation(
            new RelationBuilder()
                .withForeignKeyFields(SOURCE_SEVEN.TARGET)
                .withSourceField(SourceField.OBJECT)
                .withTargetField(TargetField.NONE)
                .withLeftSideObjectName("targetObj")
        )

        .withRelation(
            new RelationBuilder()
                .withForeignKeyFields(
                    SOURCE_EIGHT.TARGET_NAME, SOURCE_EIGHT.TARGET_NUM
                )
                .withSourceField(SourceField.OBJECT)
                .withTargetField(TargetField.MANY)
                .withLeftSideObjectName("targetEight")
                .withRightSideObjectName("sourceEights")
        )
        .withRelation(
            new RelationBuilder()
                .withPojoFields(
                    TargetNineCounts.class,
                    Collections.singletonList("targetId"),
                    TargetNine.class,
                    Collections.singletonList("id")
                )
                .withSourceField(SourceField.OBJECT)
                .withTargetField(TargetField.ONE)
        )

        .additionalQueries(GraphQLFieldDefinition.newFieldDefinition()
            .name("extraQuery")
            .type(Scalars.GraphQLString)
            .argument(
                GraphQLArgument.newArgument()
                    .name("value")
                    .type(Scalars.GraphQLString)
                    .build()
            )
            .dataFetcher(env -> "extra:" + env.getArgument("value"))
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
            .dataFetcher(env -> "mutated:" + env.getArgument("value"))
            .build()
        )

        .buildGraphQLSchema();


    {
        //log.info("QUERY TYPE: {}", schema.getQueryType());
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

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
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


        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
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

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        assertThat(
            JSON.defaultJSON().forValue(executionResult.getData()),
            is("{\"walkForwardRef\":[{\"id\":\"id1\",\"target\":{\"id\":\"id2\"}}]}")
        );
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


        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        assertThat(
            JSON.defaultJSON().forValue(executionResult.getData()),
            is("{\"walkBackOne\":{\"id\":\"target-id\",\"sourceFive\":{\"id\":\"src-id\"}}}")
        );
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

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        assertThat(
            JSON.defaultJSON().forValue(executionResult.getData()),
            is("{\"walkBackMany\":[{\"id\":\"target-id\",\"sourceSixes\":[{\"id\":\"source-id\"}," +
                "{\"id\":\"source-id2\"}]}]}")
        );
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
        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
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
        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
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


        variables.put(
            "target",
            ImmutableMap.of("name", "qwertz", "created", GraphQLTimestampScalar.toISO8601(new Timestamp(3600)))
        );
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("mutation mutateConverted($target: ConversionTargetInput!) { mutateConverted(target: $target) }")
            .variables(variables)
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);
        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        assertThat(
            JSON.defaultJSON().forValue(executionResult.getData()),
            is("{\"mutateConverted\":\"qwertz:1970-01-01 01:00:03.6\"}")
        );
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
        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        assertThat(
            JSON.defaultJSON().forValue(executionResult.getData()),
            is("{\"beanWithFetcher\":{\"value\":\"test:Value From Logic\"}}")
        );
    }


    /**
     * Test parametrized fields
     *
     */
    @Test
    public void testGetterArguments()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(dslContext)
            .logicBeans(Collections.singletonList(new GetterArgLogic()))
            .buildGraphQLSchema();

        //log.info(((GraphQLObjectType)schema.getType("GetterArgBean")).getFieldDefinition("modifiedValue").toString());

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
            assertThat(executionResult.getErrors(), is(Collections.emptyList()));
            assertThat(
                JSON.defaultJSON().forValue(executionResult.getData()),
                is("{\"getterArgBean\":{\"modifiedValue\":\"Value From GetterArgLogic:aaa:12\"}}")
            );
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
            assertThat(executionResult.getErrors(), is(Collections.emptyList()));
            assertThat(
                JSON.defaultJSON().forValue(executionResult.getData()),
                is("{\"getterArgBean\":{\"modifiedValue\":\"Value From GetterArgLogic:bbb:1111\"}}")
            );
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
            assertThat(executionResult.getErrors(), is(Collections.emptyList()));
            assertThat(
                JSON.defaultJSON().forValue(executionResult.getData()),
                is("{\"getterArgBean\":{\"introduced\":{\"name\":\"ccc\"}}}")
            );
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
                assertThat(executionResult.getErrors(), is(Collections.emptyList()));
                assertThat(
                    JSON.defaultJSON().forValue(executionResult.getData()),
                    is("{\"queryWithEnumArg\":\"(C)\"}")
                );
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
                assertThat(executionResult.getErrors(), is(Collections.emptyList()));
                assertThat(
                    JSON.defaultJSON().forValue(executionResult.getData()),
                    is("{\"queryWithObjectArgWithEnum\":\"BeanWithEnum: anotherEnum = X\"}")
                );
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
                assertThat(executionResult.getErrors(), is(Collections.emptyList()));
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
                assertThat(executionResult.getErrors(), is(Collections.emptyList()));
                assertThat(
                    JSON.defaultJSON().forValue(executionResult.getData()),
                    is("{\"objectWithEnumMutation\":{\"anotherEnum\":\"Z\"}}")
                );
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
            assertThat(executionResult.getErrors(), is(Collections.emptyList()));
            assertThat(JSON.defaultJSON().forValue(executionResult.getData()), is("{\"fullQuery\":true}"));

            assertThat(
                JSONUtil.DEFAULT_GENERATOR.forValue(domainQLExecutionContext.getResponse()),
                is("{\"name\":\"Blafusel\",\"num\":12948}")
            );
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
            assertThat(
                errors.get(0).getMessage(),
                containsString("Query 'fullQuery' is annotated with (full=true) and cannot be queried without @full")
            );
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
        assertThat(
            errors.get(0).getMessage(),
            containsString(
                " A new de.quinscape.domainql.DomainQLExecutionContext instance or subclass must be provided as " +
                    ".context() in the GraphQL endpoint")
        );
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


        assertThat(executionResult.getErrors(), is(Collections.emptyList()));

        final Map<String, Object> m = (Map<String, Object>) ((Map<String, Object>) executionResult.getData()).get(
            "getPayload");

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
        assertThat(sourceOneInput.getField("rowCount").getType(), is(nonNull(Scalars.GraphQLInt)));
        assertThat(sourceOneInput.getField("rows").getType(),
            is(nonNull(new GraphQLList(schema.getType("PayloadInput")))));


        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        Map<String, Object> payloadData = JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
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

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        final Map<String, Object> data = executionResult.getData();

        assertThat(data.get("mutationWithDegenerifiedInput"), is("AAA:100|BBB:101|"));

    }


    @Test
    public void testDegenerifiedInput()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new DegenerifiedContainerLogic()))
            .buildGraphQLSchema();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        Map<String, Object> containerData = JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
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

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        final Map<String, Object> data = executionResult.getData();

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


        assertThat(executionResult.getErrors(), is(Collections.emptyList()));

        final Map<String, Object> m = (Map<String, Object>) ((Map<String, Object>) executionResult.getData()).get(
            "queryContainer");

        assertThat(m.get("num"), is(555));
        Map<String, Object> payload = (Map<String, Object>) m.get("value");

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

        Map<String, Object> containerPropData = JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
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


        assertThat(executionResult.getErrors(), is(Collections.emptyList()));

        final String result = (String) ((Map<String, Object>) executionResult.getData()).get("mutationWithDD");

        assertThat(result, is("[EEE:666:777]"));

    }


    @Test
    public void testGenericDomainObject()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new GenericDomainLogic()))
            .withAdditionalScalar(DomainObject.class, DomainObjectScalar.newDomainObjectScalar())
            .withAdditionalInputType(Foo.class)
            .buildGraphQLSchema();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        Map<String, Object> domainObjectJSON = JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
            "    \"_type\" : \"Foo\",\n" +
            "    \"id\": \"e7c103e7-f559-4896-ac44-702b8458f207\",\n" +
            "    \"name\" : \"GreenFoo\",\n" +
            "    \"num\" : 9384,\n" +
            "    \"created\" : \"2018-10-15T14:03:58.078Z\"\n" +
            "}");
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("mutation store($domainObject: DomainObject!)\n" +
                "{\n" +
                "    store(domainObject: $domainObject)\n" +
                "}")
            .variables(ImmutableMap.of("domainObject", domainObjectJSON))
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));

        final Object data = ((Map<String, Object>) executionResult.getData()).get("store");

        assertThat(
            data,
            is("{\"_javaType\":\"de.quinscape.domainql.testdomain.tables.pojos.Foo\",\"created\":\"=2018-10-15 16:03:58.078 (Timestamp)\",\"id\":\"=e7c103e7-f559-4896-ac44-702b8458f207 (String)\",\"name\":\"=GreenFoo (String)\",\"num\":\"=9384 (Integer)\"}")
        );

    }


    private JSONPathUtil util = new JSONPathUtil(JSONUtil.OBJECT_SUPPORT);


    @Test
    public void testGenericDomainObjectOutput()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .withAdditionalScalar(DomainObject.class, DomainObjectScalar.newDomainObjectScalar())
            .logicBeans(Collections.singleton(new GenericDomainOutputLogic()))
            .buildGraphQLSchema();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("query queryDomainObject\n" +
                "{\n" +
                "    queryDomainObject\n" +
                "}")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));


        final Map<String, Object> data = (Map<String, Object>) util.getPropertyPath(
            executionResult.getData(),
            "queryDomainObject"
        );

//        log.info(JSONUtil.formatJSON(
//            JSONUtil.DEFAULT_GENERATOR.forValue(
//                data
//            )
//        ));

        assertThat(data.get("created"), is("2018-01-01T12:34:56.123Z"));

    }


    @Test
    public void testTypeParam()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TypeParamLogic()))
            .build();
        final GraphQLSchema schema = domainQL
            .getGraphQLSchema();

        final GraphQLObjectType typeA = (GraphQLObjectType) schema.getType("TypeA");
        final GraphQLObjectType typeB = (GraphQLObjectType) schema.getType("TypeB");
        assertThat(typeA,is(notNullValue()));
        assertThat(typeB,is(notNullValue()));
        assertThat(typeA.getFieldDefinition("value"),is(notNullValue()));
        assertThat(typeB.getFieldDefinition("value"),is(notNullValue()));


        final GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("query queryA($complex: ComplexInput!)\n" +
                "{\n" +
                "    queryTypeA(complexInput: $complex)\n" +
                "    {\n" +
                "        value\n" +
                "    }\n" +
                "}")
            .variables(ImmutableMap.of("complex", JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
                "    \"value\" : \"cvA\",\n" +
                "    \"num\" : 2984\n" +
                "}")))
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));

        Map data = executionResult.getData();
        //log.info(JSONUtil.DEFAULT_GENERATOR.forValue(data));
        assertThat(util.getPropertyPath(data, "queryTypeA.value"), is("cvA/2984"));
    }


    @Test
    public void testTypeParam2()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TypeParamLogic()))
            .build();
        final GraphQLSchema schema = domainQL
            .getGraphQLSchema();

        final GraphQLObjectType typeA = (GraphQLObjectType) schema.getType("TypeA");
        final GraphQLObjectType typeB = (GraphQLObjectType) schema.getType("TypeB");
        assertThat(typeA,is(notNullValue()));
        assertThat(typeB,is(notNullValue()));
        assertThat(typeA.getFieldDefinition("value"),is(notNullValue()));
        assertThat(typeB.getFieldDefinition("value"),is(notNullValue()));

        final GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        {
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("query queryB($complex: ComplexInput)\n" +
                    "{\n" +
                    "    queryTypeB(complexInput: $complex)\n" +
                    "    {\n" +
                    "        value\n" +
                    "    }\n" +
                    "}")
                .variables(ImmutableMap.of("complex", JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
                    "    \"value\" : \"cvB\",\n" +
                    "    \"num\" : 1828\n" +
                    "}")))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            Map data = executionResult.getData();
            //log.info(JSONUtil.DEFAULT_GENERATOR.forValue(data));
            assertThat(util.getPropertyPath(data, "queryTypeB.value"), is("cvB/1828"));
        }


        {
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("query queryContainerTypeA($complex: ComplexInput)\n" +
                    "{\n" +
                    "    queryContainerTypeA(complexInput: $complex)\n" +
                    "    {\n" +
                    "        value{\n" +
                    "            value\n" +
                    "        }\n" +
                    "            num\n" +
                    "    }\n" +
                    "}")
                .variables(ImmutableMap.of("complex", JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
                    "    \"value\" : \"cvA\",\n" +
                    "    \"num\" : 8283\n" +
                    "}")))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            Map data = executionResult.getData();
            //log.info(JSONUtil.DEFAULT_GENERATOR.forValue(data));
            assertThat(util.getPropertyPath(data, "queryContainerTypeA.value.value"), is("cvA/8283"));
            assertThat(util.getPropertyPath(data, "queryContainerTypeA.num"), is(123));
        }


        {
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("query queryContainerTypeB($complex: ComplexInput!)\n" +
                    "{\n" +
                    "    queryContainerTypeB(complexInput: $complex)\n" +
                    "    {\n" +
                    "        value{\n" +
                    "            value\n" +
                    "        }\n" +
                    "            num\n" +
                    "    }\n" +
                    "}")
                .variables(ImmutableMap.of("complex", JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
                    "    \"value\" : \"cvB\",\n" +
                    "    \"num\" : 2534\n" +
                    "}")))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            Map data = executionResult.getData();
            //log.info(JSONUtil.DEFAULT_GENERATOR.forValue(data));
            assertThat(util.getPropertyPath(data, "queryContainerTypeB.value.value"), is("cvB/2534"));
            assertThat(util.getPropertyPath(data, "queryContainerTypeB.num"), is(123));
        }


        {
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("query queryListTypeA($complex: ComplexInput!)\n" +
                    "{\n" +
                    "    queryListTypeA(complexInput: $complex)\n" +
                    "    {\n" +
                    "        value\n" +
                    "    }\n" +
                    "}")
                .variables(ImmutableMap.of("complex", JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
                    "    \"value\" : \"lll\",\n" +
                    "    \"num\" : 9284\n" +
                    "}")))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            Map data = executionResult.getData();
            //log.info(JSONUtil.DEFAULT_GENERATOR.forValue(data));
            assertThat(util.getPropertyPath(data, "queryListTypeA[0].value"), is("lll..."));
            assertThat(util.getPropertyPath(data, "queryListTypeA[1].value"), is("...9284"));
        }


    }


    @Test
    public void testTypeParamForMutation()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TypeParamMutationLogic()))
            .build();
        final GraphQLSchema schema = domainQL
            .getGraphQLSchema();

        final GraphQLObjectType typeA = (GraphQLObjectType) schema.getType("TypeA");
        final GraphQLObjectType typeB = (GraphQLObjectType) schema.getType("TypeB");
        assertThat(typeA,is(notNullValue()));
        assertThat(typeB,is(notNullValue()));
        assertThat(typeA.getFieldDefinition("value"),is(notNullValue()));
        assertThat(typeB.getFieldDefinition("value"),is(notNullValue()));

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        {
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("mutation mutateA($complex: ComplexInput!)\n" +
                    "{\n" +
                    "    mutateTypeA(complexInput: $complex)\n" +
                    "    {\n" +
                    "        value\n" +
                    "    }\n" +
                    "}")
                .variables(ImmutableMap.of("complex", JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
                    "    \"value\" : \"cvA\",\n" +
                    "    \"num\" : 2984\n" +
                    "}")))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            Map data = executionResult.getData();
            //log.info(JSONUtil.DEFAULT_GENERATOR.forValue(data));
            assertThat(util.getPropertyPath(data, "mutateTypeA.value"), is("cvA/2984"));
        }

        {
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("mutation mutateB($complex: ComplexInput!)\n" +
                    "{\n" +
                    "    mutateTypeB(complexInput: $complex)\n" +
                    "    {\n" +
                    "        value\n" +
                    "    }\n" +
                    "}")
                .variables(ImmutableMap.of("complex", JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
                    "    \"value\" : \"cvB\",\n" +
                    "    \"num\" : 1828\n" +
                    "}")))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            Map data = executionResult.getData();
            //log.info(JSONUtil.DEFAULT_GENERATOR.forValue(data));
            assertThat(util.getPropertyPath(data, "mutateTypeB.value"), is("cvB/1828"));
        }

        {
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("mutation mutateContainerTypeA($complex: ComplexInput!)\n" +
                    "{\n" +
                    "    mutateContainerTypeA(complexInput: $complex)\n" +
                    "    {\n" +
                    "        value{\n" +
                    "            value\n" +
                    "        }\n" +
                    "            num\n" +
                    "    }\n" +
                    "}")
                .variables(ImmutableMap.of("complex", JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
                    "    \"value\" : \"cvA\",\n" +
                    "    \"num\" : 8283\n" +
                    "}")))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            Map data = executionResult.getData();
            //log.info(JSONUtil.DEFAULT_GENERATOR.forValue(data));
            assertThat(util.getPropertyPath(data, "mutateContainerTypeA.value.value"), is("cvA/8283"));
            assertThat(util.getPropertyPath(data, "mutateContainerTypeA.num"), is(123));
        }

        {
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("mutation mutateContainerTypeB($complex: ComplexInput!)\n" +
                    "{\n" +
                    "    mutateContainerTypeB(complexInput: $complex)\n" +
                    "    {\n" +
                    "        value{\n" +
                    "            value\n" +
                    "        }\n" +
                    "            num\n" +
                    "    }\n" +
                    "}")
                .variables(ImmutableMap.of("complex", JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
                    "    \"value\" : \"cvB\",\n" +
                    "    \"num\" : 2534\n" +
                    "}")))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            Map data = executionResult.getData();
            //log.info(JSONUtil.DEFAULT_GENERATOR.forValue(data));
            assertThat(util.getPropertyPath(data, "mutateContainerTypeB.value.value"), is("cvB/2534"));
            assertThat(util.getPropertyPath(data, "mutateContainerTypeB.num"), is(123));
        }

        {
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("mutation mutateListTypeA($complex: ComplexInput!)\n" +
                    "{\n" +
                    "    mutateListTypeA(complexInput: $complex)\n" +
                    "    {\n" +
                    "        value\n" +
                    "    }\n" +
                    "}")
                .variables(ImmutableMap.of("complex", JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
                    "    \"value\" : \"lll\",\n" +
                    "    \"num\" : 9284\n" +
                    "}")))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            Map data = executionResult.getData();
            //log.info(JSONUtil.DEFAULT_GENERATOR.forValue(data));
            assertThat(util.getPropertyPath(data, "mutateListTypeA[0].value"), is("lll..."));
            assertThat(util.getPropertyPath(data, "mutateListTypeA[1].value"), is("...9284"));
        }
    }


    @Test
    public void testNullForComplexValue()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new NullForComplexValueLogic()))
            .build();
        final GraphQLSchema schema = domainQL
            .getGraphQLSchema();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        //log.info(domainQL.getFieldLookup().toString());
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query(
                // language=GraphQL
                "{\n" +
                "    logicWithComplexInput\n" +
                "}")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));


    }

    @Test
    public void testAccessDomainQLByEnv()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new AccessDomainQLLogic()))
            .build();
        final GraphQLSchema schema = domainQL
            .getGraphQLSchema();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        //log.info(domainQL.getFieldLookup().toString());
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query(
                // language=GraphQL
                "{\n" +
                "    accessDomainQLLogic\n" +
                "}")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));

        Map<String,Object> data = executionResult.getData();
        assertThat(data.get("accessDomainQLLogic"), is(true));

    }

    @Test
    public void testGenericScalar()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new GenericScalarLogic()))
            .withAdditionalScalar(GenericScalar.class, GenericScalarType.newGenericScalar())
            .withAdditionalScalar(DomainObject.class, DomainObjectScalar.newDomainObjectScalar())
            .buildGraphQLSchema();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        {
            Map<String, Object> scalarJSON = JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
                "    \"type\" : \"Int\",\n" +
                "    \"value\" : 29378\n" +
                "}");
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("mutation genericScalarLogic($value: GenericScalar!)\n" +
                    "{\n" +
                    "    genericScalarLogic(value: $value)\n" +
                    "}")
                .variables(ImmutableMap.of("value", scalarJSON))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            final Map<String, Object> data = (Map<String, Object>) ((Map<String, Object>) executionResult.getData()).get("genericScalarLogic");

            assertThat(data.get("type"), is ("Int"));
            assertThat(data.get("value"), is (29379));

        }

        {
            Map<String, Object> scalarJSON = JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
                "    \"type\" : \"Timestamp\",\n" +
                "    \"value\" : \"1970-01-01T01:00:00.000Z\"\n" +
                "}");
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("mutation genericScalarLogic($value: GenericScalar!)\n" +
                    "{\n" +
                    "    genericScalarLogic(value: $value)\n" +
                    "}")
                .variables(ImmutableMap.of("value", scalarJSON))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            final Map<String, Object> data = (Map<String, Object>) ((Map<String, Object>) executionResult.getData()).get("genericScalarLogic");

            assertThat(data.get("type"), is ("Timestamp"));
            assertThat(data.get("value"), is ("1970-01-01T02:00:00.000Z"));

        }

        {
            final Map inputJSON = JSONUtil.DEFAULT_PARSER.parse(Map.class, "{\n" +
                "    \"type\": \"[Int]\",\n" +
                "    \"value\": [1, 3, 5]\n" +
                "}");
            Map<String, Object> scalarJSON = (Map<String, Object>) inputJSON;
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("query genericList($input: GenericScalar!)\n" +
                    "{\n" +
                    "    genericList(input: $input)\n" +
                    "}")
                .variables(ImmutableMap.of("input", scalarJSON))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            final Map<String, Object> data = (Map<String, Object>) ((Map<String, Object>) executionResult.getData()).get("genericList");

            assertThat(data.get("value"), is (Arrays.asList(3,9,15)));

        }
    }


    @Test
    public void testFetcherContext()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(dslContext)
            .objectTypes(Public.PUBLIC)
            .logicBeans(new FetcherContextLogic())

            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_THREE.TARGET_ID)
                    .withSourceField(SourceField.OBJECT)
            )

            .buildGraphQLSchema();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("query sourceThreeWithFetcherContext\n" +
                "{\n" +
                "    sourceThreeWithFetcherContext\n" +
                "    {\n" +
                "        id\n" +
                "        target\n" +
                "        {\n" +
                "            id\n" +
                "        }\n" +
                "    }\n" +
                "}")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));

        final Map<String, Object> data = (Map<String, Object>) ((Map<String, Object>) executionResult.getData()).get(
            "sourceThreeWithFetcherContext");

        final JSONPathUtil util = new JSONPathUtil(JSONUtil.OBJECT_SUPPORT);

        assertThat(data.get(DomainObject.ID), is("source-three-0001"));
        assertThat(util.getPropertyPath(data, "target.id"), is("target-three-fetch-context"));
    }


    @Test
    public void testToOneBackReferenceWithFetcherContext()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(dslContext)
            .objectTypes(Public.PUBLIC)
            .logicBeans(new FetcherContextLogic())

            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_FIVE.TARGET_ID)
                    .withSourceField(SourceField.NONE)
                    .withTargetField(TargetField.ONE)
            )

            .buildGraphQLSchema();


        // fetcher context back reference *-to-one
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("query sourceThreeWithFetcherContext\n" +
                "{\n" +
                "    targetFiveWithFetcherContext\n" +
                "    {\n" +
                "        id\n" +
                "        sourceFive\n" +
                "        {\n" +
                "            id\n" +
                "        }\n" +
                "    }\n" +
                "}")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));

        final Map<String, Object> data = (Map<String, Object>) ((Map<String, Object>) executionResult.getData()).get(
            "targetFiveWithFetcherContext");

        final JSONPathUtil util = new JSONPathUtil(JSONUtil.OBJECT_SUPPORT);

        assertThat(data.get(DomainObject.ID), is("target-five-0001"));
        assertThat(util.getPropertyPath(data, "sourceFive.id"), is("source-five-fetch-context"));

    }
    @Test
    public void testToManyBackReferenceWithFetcherContext()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(dslContext)
            .objectTypes(Public.PUBLIC)
            .logicBeans(new FetcherContextLogic())

            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_SIX.TARGET_ID)
                    .withSourceField(SourceField.NONE)
                    .withTargetField(TargetField.MANY)
            )

            .buildGraphQLSchema();

        // fetcher context back reference *-to-many
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("query sourceThreeWithFetcherContext\n" +
                "{\n" +
                "    targetSixWithFetcherContext\n" +
                "    {\n" +
                "        id\n" +
                "        sourceSixes\n" +
                "        {\n" +
                "            id\n" +
                "        }\n" +
                "    }\n" +
                "}")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));

        final Map<String, Object> data = (Map<String, Object>) ((Map<String, Object>) executionResult.getData()).get(
            "targetSixWithFetcherContext");

        final JSONPathUtil util = new JSONPathUtil(JSONUtil.OBJECT_SUPPORT);

        assertThat(data.get(DomainObject.ID), is("target-six-0001"));
        assertThat(util.getPropertyPath(data, "sourceSixes[0].id"), is("source-six-fetch-context"));

    }



    @Test
    public void testDBView()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new SumPerMonthLogic()))
            .objectType(SumPerMonth.class)
            .build();

        GraphQL graphQL = GraphQL.newGraphQL(domainQL.getGraphQLSchema()).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("query getSumPerMonthLogic($sum: Int!)\n" +
                "{\n" +
                "    getSumPerMonthLogic(sum: $sum)\n" +
                "    {\n" +
                "        year\n" +
                "        month\n" +
                "        sum\n" +
                "    }\n" +
                "}")
            .variables(ImmutableMap.of("sum", 1123))
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));

        final Map<String, Object> data = (Map<String, Object>) ((Map<String, Object>) executionResult.getData()).get("getSumPerMonthLogic");

        assertThat(data.get("year"), is(2019));
        assertThat(data.get("month"), is(6));
        assertThat(data.get("sum"), is(1123));

    }


    @Test
    public void testMultiKeyFKTraversal()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("{ walkMultiKey { id targetEight { id name num } } }")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        assertThat(
            JSON.defaultJSON().forValue(executionResult.getData()),
            is("{\"walkMultiKey\":[{\"id\":\"source-id\",\"targetEight\":{\"id\":\"target-id\",\"name\":\"target-name\",\"num\":123}}]}")
        );
    }


    @Test
    public void testMultiKeyFKTraversalBackwards()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("{ walkMultiKeyBackWards { id name num sourceEights { id } } }")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        assertThat(
            JSON.defaultJSON().forValue(executionResult.getData()),
            is("{\"walkMultiKeyBackWards\":[{\"id\":\"target-id\",\"name\":\"target-name\",\"num\":345,\"sourceEights\":[{\"id\":\"target-id\"},{\"id\":\"target-id-2\"}]}]}")
        );
    }


    @Test
    public void testViewPojoRelation()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("{ walkViewPojoRelation { count target { id name } } }")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        assertThat(
            JSON.defaultJSON().forValue(executionResult.getData()),
            is("{\"walkViewPojoRelation\":[{\"count\":123,\"target\":{\"id\":\"target-id\",\"name\":\"target 9 name\"}}]}")
        );
    }


    @Test
    public void testViewPojoRelationBackwards()
    {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("{ walkViewPojoRelationBackwards { id name targetNineCounts { count } } }")
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        assertThat(
            JSON.defaultJSON().forValue(executionResult.getData()),
            is("{\"walkViewPojoRelationBackwards\":[{\"id\":\"target-id\",\"name\":\"target 9 name\",\"targetNineCounts\":{\"count\":234}}]}")
        );
    }

    @Test
    public void testListOfDomainObjectScalarsInput()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new ListInputLogic()))
            .withAdditionalInputType(Foo.class)
            .withAdditionalScalar(DomainObject.class, DomainObjectScalar.newDomainObjectScalar())
            .build();

        //log.info(new SchemaPrinter().print(domainQL.getGraphQLSchema()));

        GraphQL graphQL = GraphQL.newGraphQL(domainQL.getGraphQLSchema()).build();

        List<Object> listOfDomainObjects = JSONUtil.DEFAULT_PARSER.parse(List.class, "[{\n" +
            "    \"_type\" : \"Foo\",\n" +
            "    \"id\": \"1612ed5a-3d49-41e9-b087-0d9c6e2d2e90\",\n" +
            "    \"name\" : \"PurpleFoo\",\n" +
            "    \"num\" : 2847,\n" +
            "    \"created\" : \"2011-10-15T14:03:58.078" +
            "Z\"\n" +
            "},{\n" +
            "    \"_type\" : \"Foo\",\n" +
            "    \"id\": \"67c00a5a-ef33-4897-9e45-b4f0007e9f77\",\n" +
            "    \"name\" : \"PinkFoo\",\n" +
            "    \"num\" : 89578,\n" +
            "    \"created\" : \"2012-10-15T14:03:58.078Z\"\n" +
            "}" +
            "]");

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("mutation testListOfDomainObjectScalars($domainObjects: [DomainObject]!)\n" +
                "{\n" +
                "    testListOfDomainObjectScalars(domainObjects: $domainObjects)\n" +
                "}")
            .variables(ImmutableMap.of("domainObjects", listOfDomainObjects))
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));

        final String data = (String) ((Map<String, Object>) executionResult.getData()).get("testListOfDomainObjectScalars");


        assertThat(data, is("[Foo (1612ed5a-3d49-41e9-b087-0d9c6e2d2e90, PurpleFoo, 2847, 2011-10-15 16:03:58.078), Foo (67c00a5a-ef33-4897-9e45-b4f0007e9f77, PinkFoo, 89578, 2012-10-15 16:03:58.078)]"));

    }

    @Test
    public void testListOfDomainObjectsInput()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new ListInputLogic()))
            .withAdditionalInputType(Foo.class)
            .withAdditionalScalar(DomainObject.class, DomainObjectScalar.newDomainObjectScalar())
            .build();

        //log.info(new SchemaPrinter().print(domainQL.getGraphQLSchema()));

        GraphQL graphQL = GraphQL.newGraphQL(domainQL.getGraphQLSchema()).build();

        List<Object> listOfDomainObjects = JSONUtil.DEFAULT_PARSER.parse(List.class, "[{\n" +
            "    \"value\" : \"Complex A\",\n" +
            "    \"num\" : 3098\n" +
            "},{\n" +
            "    \"value\" : \"Complex B\",\n" +
            "    \"num\" : -20487\n" +
            "}" +
            "]");

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("mutation testListOfDomainObjects($complexInputs: [ComplexInput]!)\n" +
                "{\n" +
                "    testListOfDomainObjects(complexInputs: $complexInputs)\n" +
                "}")
            .variables(ImmutableMap.of("complexInputs", listOfDomainObjects))
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));

        final String data = (String) ((Map<String, Object>) executionResult.getData()).get("testListOfDomainObjects");


        assertThat(data, is("ComplexInput: value = 'Complex A', num = 3098|ComplexInput: value = 'Complex B', num = -20487|"));

    }

    @Test
    public void testListOfEnumsInput()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new ListInputLogic()))
            .withAdditionalInputType(Foo.class)
            .withAdditionalScalar(DomainObject.class, DomainObjectScalar.newDomainObjectScalar())
            .build();

        //log.info(new SchemaPrinter().print(domainQL.getGraphQLSchema()));

        GraphQL graphQL = GraphQL.newGraphQL(domainQL.getGraphQLSchema()).build();

        List<Object> listOfDomainObjects = JSONUtil.DEFAULT_PARSER.parse(List.class, "[\"B\",\"C\",\"A\"]");

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("mutation testListOfEnums($enums: [MyEnum]!)\n" +
                "{\n" +
                "    testListOfEnums(enums: $enums)\n" +
                "}")
            .variables(ImmutableMap.of("enums", listOfDomainObjects))
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));

        final String data = (String) ((Map<String, Object>) executionResult.getData()).get("testListOfEnums");


        assertThat(data, is("B|C|A|"));

    }

    @Test
    public void testNullableListOfScalarsInput()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new ListInputLogic()))
            .withAdditionalInputType(Foo.class)
            .withAdditionalScalar(DomainObject.class, DomainObjectScalar.newDomainObjectScalar())
            .build();

        //log.info(new SchemaPrinter().print(domainQL.getGraphQLSchema()));

        GraphQL graphQL = GraphQL.newGraphQL(domainQL.getGraphQLSchema()).build();

        {
            List<Object> listOfStrings = JSONUtil.DEFAULT_PARSER.parse(List.class, "[\"AA\", \"BBB\", \"CCCC\"]");

            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("mutation testNullableListOfScalars($strings: [String])\n" +
                    "{\n" +
                    "    testNullableListOfScalars(strings: $strings)\n" +
                    "}")
                .variables(ImmutableMap.of("strings", listOfStrings))
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            final String data = (String) ((Map<String, Object>) executionResult.getData()).get("testNullableListOfScalars");


            assertThat(data, is("[AA, BBB, CCCC]"));
        }

        {

            final HashMap<String, Object> variables = new HashMap<>();
            variables.put("strings", null);
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("mutation testNullableListOfScalars($strings: [String])\n" +
                    "{\n" +
                    "    testNullableListOfScalars(strings: $strings)\n" +
                    "}")
                .variables(variables)
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            final String data = (String) ((Map<String, Object>) executionResult.getData()).get("testNullableListOfScalars");


            assertThat(data, is("null"));
        }
    }

    @Test
    public void testListOfGenericDomainObjects()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new GenericScalarLogic()))
            .withAdditionalScalar(GenericScalar.class, GenericScalarType.newGenericScalar())
            .withAdditionalScalar(DomainObject.class, DomainObjectScalar.newDomainObjectScalar())
            .build();

        log.info("TYPES = {}", domainQL.getGraphQLSchema().getType("DomainObject"));

        GraphQL graphQL = GraphQL.newGraphQL(domainQL.getGraphQLSchema()).build();

        {
            final String now = "2020-04-29T16:14:53.173Z";
            final HashMap<String, Object> variables = new HashMap<>();
            variables.put("time", now);

            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                // language=GraphQL
                .query("query deserializeObjs($time: Timestamp)\n" +
                    "{\n" +
                    "    deserializeObjs(time: $time)\n" +
                    "}")
                .variables(variables)
                .build();

            ExecutionResult executionResult = graphQL.execute(executionInput);

            assertThat(executionResult.getErrors(), is(Collections.emptyList()));

            final Map<String, Object> data = (Map<String, Object>) ((Map<String, Object>) executionResult.getData()).get("deserializeObjs");

            assertThat(data.get("type"), is ("[DomainObject]"));
            final List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("value");
            assertThat(list.size(), is (2));

            assertThat(list.get(0).get("name"), is("Foo #1"));
            assertThat(list.get(0).get("num"), is(1001));
            assertThat(list.get(0).get("created"), is(now));

            assertThat(list.get(1).get("name"), is("Foo #2"));
            assertThat(list.get(1).get("num"), is(1002));
            assertThat(list.get(1).get("created"), is(now));

        }
    }
}
