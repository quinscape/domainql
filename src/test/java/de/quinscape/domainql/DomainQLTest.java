package de.quinscape.domainql;


import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.scalar.GraphQLTimestampScalar;
import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static graphql.schema.GraphQLNonNull.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DomainQLTest
{
    private final static Logger log = LoggerFactory.getLogger(DomainQLTest.class);

    final TestLogic logic = new TestLogic();

    final GraphQLSchema schema = DomainQL.newDomainQL(null)
        .objectTypes(Public.PUBLIC)
        .logicBeans(Collections.singleton(logic))

        // source variants
        .configureRelation(Keys.SOURCE_ONE__FK_SOURCE_ONE_TARGET_ID, SourceField.NONE, TargetField.NONE)
        .configureRelation(Keys.SOURCE_TWO__FK_SOURCE_TWO_TARGET_ID, SourceField.SCALAR, TargetField.NONE)
        .configureRelation(Keys.SOURCE_THREE__FK_SOURCE_THREE_TARGET_ID, SourceField.OBJECT, TargetField.NONE)

        // target variants
        .configureRelation(Keys.SOURCE_FIVE__FK_SOURCE_FIVE_TARGET_ID, SourceField.NONE, TargetField.ONE)
        .configureRelation(Keys.SOURCE_SIX__FK_SOURCE_SIX_TARGET_ID, SourceField.NONE, TargetField.MANY)

        .buildGraphQLSchema();


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
            assertThat(queryString2.getArgument("value").getType(), is(nonNull(Scalars.GraphQLString)));
            assertThat(queryString2.getArgument("second").getType(), is(nonNull(Scalars.GraphQLString)));
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
            assertThat(queryTimestamp.getType(), is(instanceOf(GraphQLTimestampScalar.class)));
        }

        {
            final GraphQLFieldDefinition queryWithComplexInput = schema.getQueryType().getFieldDefinition("queryWithComplexInput");

            assertThat(queryWithComplexInput, is(notNullValue()));
            assertThat(queryWithComplexInput.getArguments().size(), is(1));
            assertThat(queryWithComplexInput.getArgument("complexInput").getType().getName(), is("ComplexInput"));
            assertThat(queryWithComplexInput.getType(), is(Scalars.GraphQLBoolean));
        }
    }


    @Test
    public void testImplicitInputTypes()
    {
        // Input types of queries and mutations are automatically defined

        GraphQLInputObjectType inputType = (GraphQLInputObjectType) schema.getType("ComplexInput");
        assertThat(inputType,is(notNullValue()));
        assertThat(inputType.getFields().size(),is(2));
        assertThat(inputType.getField("value").getType(),is(nonNull(Scalars.GraphQLString)));
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
            assertThat(sourceOne.getFieldDefinitions().get(0).getName(),is("id"));
            assertThat(sourceOne.getFieldDefinitions().get(0).getType(),is(nonNull(Scalars.GraphQLString)));

            final GraphQLObjectType targetOne = (GraphQLObjectType) schema.getType("TargetOne");
            assertThat(targetOne,is(notNullValue()));
            assertThat(targetOne.getFieldDefinitions().size(),is(1));
            assertThat(targetOne.getFieldDefinitions().get(0).getName(),is("id"));
            assertThat(targetOne.getFieldDefinitions().get(0).getType(),is(nonNull(Scalars.GraphQLString)));

        }

        // SourceField.SCALAR
        {
            // just the id fields
            final GraphQLObjectType sourceTwo = (GraphQLObjectType) schema.getType("SourceTwo");
            assertThat(sourceTwo,is(notNullValue()));
            assertThat(sourceTwo.getFieldDefinitions().size(),is(2));
            assertThat(sourceTwo.getFieldDefinitions().get(1).getName(),is("targetId"));
            assertThat(sourceTwo.getFieldDefinitions().get(1).getType(),is(nonNull(Scalars.GraphQLString)));

        }


        // SourceField.OBJECT
        {
            // just the id fields
            final GraphQLObjectType sourceThree = (GraphQLObjectType) schema.getType("SourceThree");
            assertThat(sourceThree,is(notNullValue()));
            assertThat(sourceThree.getFieldDefinitions().size(),is(2));
            assertThat(sourceThree.getFieldDefinitions().get(1).getName(),is("target"));
            assertThat(sourceThree.getFieldDefinitions().get(1).getType(),is(nonNull(schema.getType("TargetThree"))));
        }

        // TargetField.ONE
        {
            // just the id fields
            final GraphQLObjectType targetFive = (GraphQLObjectType) schema.getType("TargetFive");
            assertThat(targetFive,is(notNullValue()));
            assertThat(targetFive.getFieldDefinitions().size(),is(2));
            assertThat(targetFive.getFieldDefinitions().get(1).getName(),is("sourceFive"));
            assertThat(targetFive.getFieldDefinitions().get(1).getType(),is(nonNull(schema.getType("SourceFive"))));
        }

        // TargetField.MANY
        {
            // just the id fields
            final GraphQLObjectType targetSix = (GraphQLObjectType) schema.getType("TargetSix");
            assertThat(targetSix,is(notNullValue()));
            assertThat(targetSix.getFieldDefinitions().size(),is(2));
            assertThat(targetSix.getFieldDefinitions().get(1).getName(),is("sourceSixes"));
            assertThat(targetSix.getFieldDefinitions().get(1).getType(),is(nonNull(new GraphQLList(schema.getType("SourceSix")))));
        }
    }

    @Test
    public void testRelationRenaming()
    {
        // same config as before, but with configured names for the sides that are active

        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(logic))
            .configureRelation(Keys.SOURCE_TWO__FK_SOURCE_TWO_TARGET_ID, SourceField.SCALAR, TargetField.NONE, "scalarFieldId", null)
            .configureRelation(Keys.SOURCE_THREE__FK_SOURCE_THREE_TARGET_ID, SourceField.OBJECT, TargetField.NONE, "objField", null)
            .configureRelation(Keys.SOURCE_FIVE__FK_SOURCE_FIVE_TARGET_ID, SourceField.NONE, TargetField.ONE, null, "oneObj")
            .configureRelation(Keys.SOURCE_SIX__FK_SOURCE_SIX_TARGET_ID, SourceField.NONE, TargetField.MANY, null, "manyObj")
            .createMirrorInputTypes(true)
            .buildGraphQLSchema();


        // SourceField.SCALAR
        {
            // just the id fields
            final GraphQLObjectType sourceTwo = (GraphQLObjectType) schema.getType("SourceTwo");
            assertThat(sourceTwo,is(notNullValue()));
            assertThat(sourceTwo.getFieldDefinitions().size(),is(2));
            assertThat(sourceTwo.getFieldDefinitions().get(1).getName(),is("scalarFieldId"));
            assertThat(sourceTwo.getFieldDefinitions().get(1).getType(),is(nonNull(Scalars.GraphQLString)));

        }


        // SourceField.OBJECT
        {
            // just the id fields
            final GraphQLObjectType sourceThree = (GraphQLObjectType) schema.getType("SourceThree");
            assertThat(sourceThree,is(notNullValue()));
            assertThat(sourceThree.getFieldDefinitions().size(),is(2));
            assertThat(sourceThree.getFieldDefinitions().get(1).getName(),is("objField"));
            assertThat(sourceThree.getFieldDefinitions().get(1).getType(),is(nonNull(schema.getType("TargetThree"))));
        }

        // TargetField.ONE
        {
            // just the id fields
            final GraphQLObjectType targetFive = (GraphQLObjectType) schema.getType("TargetFive");
            assertThat(targetFive,is(notNullValue()));
            assertThat(targetFive.getFieldDefinitions().size(),is(2));
            assertThat(targetFive.getFieldDefinitions().get(1).getName(),is("oneObj"));
            assertThat(targetFive.getFieldDefinitions().get(1).getType(),is(nonNull(schema.getType("SourceFive"))));
        }

        // TargetField.MANY
        {
            // just the id fields
            final GraphQLObjectType targetSix = (GraphQLObjectType) schema.getType("TargetSix");
            assertThat(targetSix,is(notNullValue()));
            assertThat(targetSix.getFieldDefinitions().size(),is(2));
            assertThat(targetSix.getFieldDefinitions().get(1).getName(),is("manyObj"));
            assertThat(targetSix.getFieldDefinitions().get(1).getType(),is(nonNull(new GraphQLList(schema.getType("SourceSix")))));
        }
    }


    @Test
    public void testInputMirrorCreation()
    {

        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(logic))
            .createMirrorInputTypes(true)
            // test override
            .inputTypes(SourceTwoInput.class)
            .configureRelation(Keys.SOURCE_ONE__FK_SOURCE_ONE_TARGET_ID, SourceField.NONE, TargetField.NONE)
            .configureRelation(Keys.SOURCE_TWO__FK_SOURCE_TWO_TARGET_ID, SourceField.SCALAR, TargetField.NONE)
            .configureRelation(Keys.SOURCE_THREE__FK_SOURCE_THREE_TARGET_ID, SourceField.OBJECT, TargetField.NONE)
            //.configureRelation(Keys.SOURCE_FOUR__FK_SOURCE_FOUR_TARGET_ID, SourceField.NONE, TargetField.NONE)
            .configureRelation(Keys.SOURCE_FIVE__FK_SOURCE_FIVE_TARGET_ID, SourceField.NONE, TargetField.ONE)
            .configureRelation(Keys.SOURCE_SIX__FK_SOURCE_SIX_TARGET_ID, SourceField.NONE, TargetField.MANY)
            .buildGraphQLSchema();


        {

            final GraphQLInputObjectType sourceOneInput = (GraphQLInputObjectType) schema.getType("SourceOneInput");
            assertThat(sourceOneInput, is(notNullValue()));

            assertThat(sourceOneInput.getFields().size(), is(2));
            assertThat(sourceOneInput.getField("id").getType(),is(nonNull(Scalars.GraphQLString)));
            assertThat(sourceOneInput.getField("targetId").getType(),is(nonNull(Scalars.GraphQLString)));
        }

        {

            final GraphQLInputObjectType sourceOneInput = (GraphQLInputObjectType) schema.getType("SourceTwoInput");
            assertThat(sourceOneInput, is(notNullValue()));

            assertThat(sourceOneInput.getFields().size(), is(3));
            assertThat(sourceOneInput.getField("id").getType(),is(nonNull(Scalars.GraphQLString)));
            assertThat(sourceOneInput.getField("targetId").getType(),is(nonNull(Scalars.GraphQLString)));
            assertThat(sourceOneInput.getField("foo").getType(),is(Scalars.GraphQLString));
        }
    }


    @Test(expected = DomainQLException.class)
    public void testFieldConflict()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(logic))
            .configureRelation(Keys.SOURCE_FOUR__FK_SOURCE_FOUR_TARGET_ID, SourceField.NONE, TargetField.ONE)
            .configureRelation(Keys.SOURCE_FOUR__FK_SOURCE_FOUR_TARGET2_ID, SourceField.NONE, TargetField.ONE)
            .buildGraphQLSchema();

    }

    @Test
    public void testFieldConflictResolution()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(logic))
            .configureRelation(Keys.SOURCE_FOUR__FK_SOURCE_FOUR_TARGET_ID, SourceField.NONE, TargetField.ONE)
            .configureRelation(Keys.SOURCE_FOUR__FK_SOURCE_FOUR_TARGET2_ID, SourceField.NONE, TargetField.ONE, null, "source2")
            .buildGraphQLSchema();

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
}
