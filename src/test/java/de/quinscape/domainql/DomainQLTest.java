package de.quinscape.domainql;


import de.quinscape.domainql.beans.LogicWithWrongInjection;
import de.quinscape.domainql.beans.LogicWithWrongInjection2;
import de.quinscape.domainql.beans.TestLogic;
import de.quinscape.domainql.beans.TestLogic2;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.beans.SourceTwoInput;
import de.quinscape.domainql.scalar.GraphQLTimestampScalar;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.SourceOne;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;

import static graphql.schema.GraphQLNonNull.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import static de.quinscape.domainql.testdomain.Tables.*;

public class DomainQLTest
{
    private final static Logger log = LoggerFactory.getLogger(DomainQLTest.class);

    final TestLogic logic = new TestLogic();
    final TestLogic2 logic2 = new TestLogic2();

    final GraphQLSchema schema = DomainQL.newDomainQL(null)
        .objectTypes(Public.PUBLIC)
        .logicBeans(Collections.singleton(logic))

        // source variants
        .configureRelation(   SOURCE_ONE.TARGET_ID, SourceField.NONE, TargetField.NONE)
        .configureRelation(   SOURCE_TWO.TARGET_ID, SourceField.SCALAR, TargetField.NONE)
        .configureRelation( SOURCE_THREE.TARGET_ID, SourceField.OBJECT, TargetField.NONE)
        .configureRelation(  SOURCE_FIVE.TARGET_ID, SourceField.NONE, TargetField.ONE)
        .configureRelation(   SOURCE_SIX.TARGET_ID, SourceField.NONE, TargetField.MANY)

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
            
            .configureRelation(   SOURCE_TWO.TARGET_ID, SourceField.SCALAR, TargetField.NONE, "scalarFieldId", null)
            .configureRelation( SOURCE_THREE.TARGET_ID, SourceField.OBJECT, TargetField.NONE, "objField", null)
            .configureRelation(  SOURCE_FIVE.TARGET_ID, SourceField.NONE, TargetField.ONE, null, "oneObj")
            .configureRelation(   SOURCE_SIX.TARGET_ID, SourceField.NONE, TargetField.MANY, null, "manyObj")
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
            .logicBeans(Arrays.asList(logic, logic2))
            .createMirrorInputTypes(true)
            // test override
            .overrideInputTypes(SourceTwoInput.class)
            .configureRelation(   SOURCE_ONE.TARGET_ID, SourceField.NONE, TargetField.NONE)
            .configureRelation(   SOURCE_TWO.TARGET_ID, SourceField.SCALAR, TargetField.NONE)
            .configureRelation( SOURCE_THREE.TARGET_ID, SourceField.OBJECT, TargetField.NONE)
            .configureRelation(  SOURCE_FIVE.TARGET_ID, SourceField.NONE, TargetField.ONE)
            .configureRelation(   SOURCE_SIX.TARGET_ID, SourceField.NONE, TargetField.MANY)
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


        {
            final GraphQLObjectType queryType = (GraphQLObjectType) schema.getType("QueryType");
            final GraphQLFieldDefinition fieldDef = queryType.getFieldDefinition("queryWithMirrorInput");

            final GraphQLArgument arg = fieldDef.getArgument("inputOne");
            assertThat(arg, is(notNullValue()));
            assertThat(arg.getType().getName(), is("SourceOneInput"));
        }

    }


    @Test(expected = DomainQLException.class)
    public void testFieldConflict()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(logic))
            .configureRelation( SOURCE_FOUR.TARGET_ID, SourceField.NONE, TargetField.ONE)
            .configureRelation(SOURCE_FOUR.TARGET2_ID, SourceField.NONE, TargetField.ONE)
            .buildGraphQLSchema();

    }

    @Test
    public void testFieldConflictResolution()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(logic))
            .configureRelation(  SOURCE_FOUR.TARGET_ID, SourceField.NONE, TargetField.ONE)
            .configureRelation( SOURCE_FOUR.TARGET2_ID, SourceField.NONE, TargetField.ONE, null, "source2")
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


    @Test
    public void testObjectAndScalarScourceFields()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(logic))

            // source variants
            .configureRelation(   SOURCE_ONE.TARGET_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)
            .buildGraphQLSchema();

        // SourceField.OBJECT_AND_SCALAR / TargetField.NONE
        {
            // just the id fields
            final GraphQLObjectType sourceOne = (GraphQLObjectType) schema.getType("SourceOne");
            assertThat(sourceOne,is(notNullValue()));
            assertThat(sourceOne.getFieldDefinitions().size(),is(3));
            assertThat(sourceOne.getFieldDefinitions().get(0).getName(),is("id"));
            assertThat(sourceOne.getFieldDefinitions().get(0).getType(),is(nonNull(Scalars.GraphQLString)));
            assertThat(sourceOne.getFieldDefinitions().get(1).getName(),is("targetId"));
            assertThat(sourceOne.getFieldDefinitions().get(1).getType(),is(nonNull(Scalars.GraphQLString)));
            assertThat(sourceOne.getFieldDefinitions().get(2).getName(),is("target"));
            assertThat(sourceOne.getFieldDefinitions().get(2).getType(),is(nonNull(schema.getType("TargetOne"))));

        }
    }


    @Test(expected =  DomainQLTypeException.class)
    public void testWrongType()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(logic))
            .overrideInputTypes(SourceOne.class)

            // source variants
            .configureRelation(   SOURCE_ONE.TARGET_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)
            .buildGraphQLSchema();
    }

    @Test(expected =  DomainQLTypeException.class)
    public void testWrongTypeAsQueryInput()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new LogicWithWrongInjection()))

            // source variants
            .configureRelation(   SOURCE_ONE.TARGET_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)
            .buildGraphQLSchema();
    }

    @Test(expected =  DomainQLTypeException.class)
    public void testRecordAsQueryInput()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new LogicWithWrongInjection2()))

            // source variants
            .configureRelation(   SOURCE_ONE.TARGET_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)
            .buildGraphQLSchema();
    }
}
