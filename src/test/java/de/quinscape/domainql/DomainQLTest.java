package de.quinscape.domainql;


import de.quinscape.domainql.config.RelationModel;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.logicimpl.LogicWithMirrorInput;
import de.quinscape.domainql.logicimpl.TestLogic;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.pojos.TargetNine;
import de.quinscape.domainql.testdomain.tables.pojos.TargetNineCounts;
import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static de.quinscape.domainql.testdomain.Tables.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DomainQLTest
{
    private final static Logger log = LoggerFactory.getLogger(DomainQLTest.class);

    final TestLogic logic = new TestLogic();
    final LogicWithMirrorInput logic2 = new LogicWithMirrorInput();


    final DomainQL domainQL = DomainQL.newDomainQL(null)
        .objectTypes(Public.PUBLIC)
        .logicBeans(Collections.singleton(logic))

        // source variants
        .withRelation(
            new RelationBuilder()
                // trigger renaming in second
                .withId("SourceTwo-target")
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
                .withId("SourceSeven-renamedTarget")
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
        .build();

    final GraphQLSchema schema = domainQL.getGraphQLSchema();

    @Test
    public void testQueries()
    {
        {
            final GraphQLFieldDefinition queryTruth = schema.getQueryType().getFieldDefinition("queryTruth");

            assertThat(queryTruth, is(notNullValue()));
            assertThat(queryTruth.getArguments().size(), is(0));
            assertThat(queryTruth.getType(), is(Scalars.GraphQLBoolean));
        }

        {
            final GraphQLFieldDefinition queryString = schema.getQueryType().getFieldDefinition("queryString");

            assertThat(queryString, is(notNullValue()));
            assertThat(queryString.getArguments().size(), is(1));
            assertThat(queryString.getArgument("value").getType(), is(Scalars.GraphQLString));
            assertThat(queryString.getType(), is(Scalars.GraphQLString));
        }

        {
            final GraphQLFieldDefinition queryString2 = schema.getQueryType().getFieldDefinition("queryString2");

            assertThat(queryString2, is(notNullValue()));
            assertThat(queryString2.getArguments().size(), is(2));
            assertThat(queryString2.getArgument("value").getType().toString(), is("String!"));
            assertThat(queryString2.getArgument("second").getType().toString(), is("String!"));
            assertThat(queryString2.getType(), is(Scalars.GraphQLString));
        }

        {
            final GraphQLFieldDefinition queryInt = schema.getQueryType().getFieldDefinition("queryInt");

            assertThat(queryInt, is(notNullValue()));
            assertThat(queryInt.getArguments().size(), is(1));
            assertThat(queryInt.getArgument("value").getType(), is(Scalars.GraphQLInt));
            assertThat(queryInt.getType(), is(Scalars.GraphQLInt));
        }

        {
            final GraphQLFieldDefinition queryTimestamp = schema.getQueryType().getFieldDefinition("queryTimestamp");

            assertThat(queryTimestamp, is(notNullValue()));
            assertThat(queryTimestamp.getArguments().size(), is(0));
            assertThat(queryTimestamp.getType(), is(instanceOf(GraphQLScalarType.class)));
        }

        {
            final GraphQLFieldDefinition queryWithComplexInput = schema.getQueryType().getFieldDefinition("queryWithComplexInput");

            assertThat(queryWithComplexInput, is(notNullValue()));
            assertThat(queryWithComplexInput.getArguments().size(), is(1));
            assertThat(((GraphQLNamedType)queryWithComplexInput.getArgument("complexInput").getType()).getName(), is("ComplexInput"));
            assertThat(queryWithComplexInput.getType(), is(Scalars.GraphQLBoolean));

            final GraphQLType complexInput = schema.getType("ComplexInput");

            //log.info("ComplexInput = {}", complexInput);

            assertThat(complexInput, is(notNullValue()));

        }
    }


    @Test
    public void testImplicitInputTypes()
    {
        // Input types of queries and mutations are automatically defined

        GraphQLInputObjectType inputType = (GraphQLInputObjectType) schema.getType("ComplexInput");
        assertThat(inputType,is(notNullValue()));
        assertThat(inputType.getFields().size(),is(2));
        assertThat(inputType.getField("value").getType().toString(),is("String!"));
        assertThat(inputType.getField("num").getType(),is(Scalars.GraphQLInt));

    }

    @Test
    public void testDefaultInputMirroring()
    {
        // no mirrors by default

        assertThat(schema.getType("SourceOneInput"), is(nullValue()));
    }


    @Test
    public void testRelationModes()
    {


        // SourceField.NONE / TargetField.NONE
        {
            // just the id fields
            final GraphQLObjectType sourceOne = (GraphQLObjectType) schema.getType("SourceOne");
            assertThat(sourceOne,is(notNullValue()));
            assertThat(sourceOne.getFieldDefinitions().size(),is(1));
            assertThat(sourceOne.getFieldDefinition(DomainObject.ID).getType().toString(),is("String!"));

            final GraphQLObjectType targetOne = (GraphQLObjectType) schema.getType("TargetOne");
            assertThat(targetOne,is(notNullValue()));
            assertThat(targetOne.getFieldDefinitions().size(),is(1));
            assertThat(targetOne.getFieldDefinition(DomainObject.ID).getType().toString(),is("String!"));

        }

        // SourceField.SCALAR
        {
            // just the id fields
            final GraphQLObjectType sourceTwo = (GraphQLObjectType) schema.getType("SourceTwo");
            assertThat(sourceTwo,is(notNullValue()));
            assertThat(sourceTwo.getFieldDefinitions().size(),is(2));
            assertThat(sourceTwo.getFieldDefinition("targetId").getType().toString(),is("String!"));

        }


        // SourceField.OBJECT
        {
            // just the id fields
            final GraphQLObjectType sourceThree = (GraphQLObjectType) schema.getType("SourceThree");
            assertThat(sourceThree,is(notNullValue()));
            assertThat(sourceThree.getFieldDefinitions().size(),is(2));
            assertThat(sourceThree.getFieldDefinition("target").getType().toString(),is("TargetThree!"));
        }

        // TargetField.ONE
        {
            // just the id fields
            final GraphQLObjectType targetFive = (GraphQLObjectType) schema.getType("TargetFive");
            assertThat(targetFive,is(notNullValue()));
            assertThat(targetFive.getFieldDefinitions().size(),is(2));
            assertThat(targetFive.getFieldDefinition("sourceFive").getType().toString(),is("SourceFive!"));
        }

        // TargetField.MANY
        {
            // just the id fields
            final GraphQLObjectType targetSix = (GraphQLObjectType) schema.getType("TargetSix");
            assertThat(targetSix,is(notNullValue()));
            assertThat(targetSix.getFieldDefinitions().size(),is(2));
            assertThat(targetSix.getFieldDefinition("sourceSixes").getType().toString(),is("[SourceSix]!"));
        }

        // NON-PK TARGET
        {
            // just the id fields
            final GraphQLObjectType targetSeven = (GraphQLObjectType) schema.getType("TargetSeven");
            assertThat(targetSeven,is(notNullValue()));
            final List<GraphQLFieldDefinition> fieldDefs = targetSeven.getFieldDefinitions();

            //log.info("fieldDefs = {}", fieldDefs);

            assertThat(fieldDefs.size(),is(2));
            assertThat(targetSeven.getFieldDefinition("name").getType().toString(), is("String!"));


            final GraphQLObjectType sourceSeven = (GraphQLObjectType) schema.getType("SourceSeven");
            assertThat(sourceSeven.getFieldDefinitions().size(), is(2));
            assertThat(sourceSeven.getFieldDefinition("targetObj").getType().toString(), is("TargetSeven!"));

        }
    }

    @Test
    public void testMutations()
    {
        {
            final GraphQLFieldDefinition mutateString = schema.getMutationType().getFieldDefinition("mutateString");

            assertThat(mutateString, is(notNullValue()));
            assertThat(mutateString.getArguments().size(), is(1));
            assertThat(mutateString.getArgument("value").getType(), is(Scalars.GraphQLString));
            assertThat(mutateString.getType(), is(Scalars.GraphQLString));
        }
    }


    @Test
    public void testMultiFieldForeignKey()
    {
        //log.info(new SchemaPrinter().print(schema));

        {
            final GraphQLObjectType sourceEight = (GraphQLObjectType) schema.getType("SourceEight");
            assertThat(sourceEight.getFieldDefinitions().size(), is(2));

            assertThat(sourceEight.getFieldDefinition("id").getType().toString(), is("String!"));
            assertThat(sourceEight.getFieldDefinition("targetEight").getType().toString(), is( "TargetEight!"));
        }

        {
            final GraphQLObjectType targetEight = (GraphQLObjectType) schema.getType("TargetEight");
            assertThat(targetEight.getFieldDefinitions().size(), is(4));

            assertThat(targetEight.getFieldDefinition("id").getType().toString(), is("String!"));
            assertThat(targetEight.getFieldDefinition("name").getType().toString(), is("String!"));
            assertThat(targetEight.getFieldDefinition("num").getType().toString(), is("Int!"));
            assertThat(targetEight.getFieldDefinition("sourceEights").getType().toString(), is("[SourceEight]!"));
        }
    }


    @Test
    public void testRelationOnViewPojo()
    {

        //log.info(new SchemaPrinter().print(schema));

        final GraphQLObjectType targetNineCountsType = (GraphQLObjectType) schema.getType("TargetNineCounts");
        assertThat(targetNineCountsType.getFieldDefinitions().size(), is(2));

        // the "NOT NULL" on the original target_id field gets lost in the view
        assertThat(targetNineCountsType.getFieldDefinition("target").getType(), is(schema.getType("TargetNine")));
        assertThat(targetNineCountsType.getFieldDefinition("count").getType(), is(schema.getType("Long")));


    }


    @Test
    public void testRelationModels()
    {
        final List<RelationModel> relationModels = domainQL.getRelationModels();
        //log.info(JSONUtil.formatJSON(JSONUtil.DEFAULT_GENERATOR.forValue(relationModels)));

        assertThat(relationModels.get(0).getId(), is("SourceTwo-target"));
        assertThat(relationModels.get(1).getId(), is("SourceTwo-target2"));
        assertThat(relationModels.get(5).getId(), is("SourceSeven-renamedTarget"));

    }
}
