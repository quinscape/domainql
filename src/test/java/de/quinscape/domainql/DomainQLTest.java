package de.quinscape.domainql;


import de.quinscape.domainql.beans.LogicWithMirrorInput;
import de.quinscape.domainql.beans.TestLogic;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.scalar.GraphQLTimestampScalar;
import de.quinscape.domainql.testdomain.Public;
import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static de.quinscape.domainql.testdomain.Tables.*;
import static graphql.schema.GraphQLNonNull.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DomainQLTest
{
    private final static Logger log = LoggerFactory.getLogger(DomainQLTest.class);

    final TestLogic logic = new TestLogic();
    final LogicWithMirrorInput logic2 = new LogicWithMirrorInput();

    final GraphQLSchema schema = DomainQL.newDomainQL(null)
        .objectTypes(Public.PUBLIC)
        .logicBeans(Collections.singleton(logic))

        // source variants
        .configureRelation(   SOURCE_ONE.TARGET_ID, SourceField.NONE, TargetField.NONE)
        .configureRelation(   SOURCE_TWO.TARGET_ID, SourceField.SCALAR, TargetField.NONE)
        .configureRelation( SOURCE_THREE.TARGET_ID, SourceField.OBJECT, TargetField.NONE)
        .configureRelation(  SOURCE_FIVE.TARGET_ID, SourceField.NONE, TargetField.ONE)
        .configureRelation(   SOURCE_SIX.TARGET_ID, SourceField.NONE, TargetField.MANY)
        .configureRelation(    SOURCE_SEVEN.TARGET, SourceField.OBJECT, TargetField.NONE)

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

            final GraphQLType complexInput = schema.getType("ComplexInput");

            log.info("ComplexInput = {}", complexInput);

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
            assertThat(sourceThree.getFieldDefinition("target").getType(),is(nonNull(schema.getType("TargetThree"))));
        }

        // TargetField.ONE
        {
            // just the id fields
            final GraphQLObjectType targetFive = (GraphQLObjectType) schema.getType("TargetFive");
            assertThat(targetFive,is(notNullValue()));
            assertThat(targetFive.getFieldDefinitions().size(),is(2));
            assertThat(targetFive.getFieldDefinition("sourceFive").getType(),is(nonNull(schema.getType("SourceFive"))));
        }

        // TargetField.MANY
        {
            // just the id fields
            final GraphQLObjectType targetSix = (GraphQLObjectType) schema.getType("TargetSix");
            assertThat(targetSix,is(notNullValue()));
            assertThat(targetSix.getFieldDefinitions().size(),is(2));
            assertThat(targetSix.getFieldDefinition("sourceSixes").getType(),is(nonNull(new GraphQLList(schema.getType("SourceSix")))));
        }

        // NON-PK TARGET
        {
            // just the id fields
            final GraphQLObjectType targetSeven = (GraphQLObjectType) schema.getType("TargetSeven");
            assertThat(targetSeven,is(notNullValue()));
            final List<GraphQLFieldDefinition> fieldDefs = targetSeven.getFieldDefinitions();

            log.info("fieldDefs = {}", fieldDefs);

            assertThat(fieldDefs.size(),is(2));
            assertThat(targetSeven.getFieldDefinition("name").getType(), is(nonNull(Scalars.GraphQLString)));


            final GraphQLObjectType sourceSeven = (GraphQLObjectType) schema.getType("SourceSeven");
            assertThat(sourceSeven.getFieldDefinitions().size(), is(2));
            assertThat(sourceSeven.getFieldDefinition("target").getType(), is(nonNull(schema.getType("TargetSeven"))));

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

}
