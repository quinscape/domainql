package de.quinscape.domainql;

import de.quinscape.domainql.beans.GenericScalarLogic;
import de.quinscape.domainql.beans.SourceSeven;
import de.quinscape.domainql.beans.SumPerMonth;
import de.quinscape.domainql.beans.TargetSeven;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.generic.DomainObjectScalar;
import de.quinscape.domainql.generic.GenericScalar;
import de.quinscape.domainql.generic.GenericScalarType;
import de.quinscape.domainql.logicimpl.BigNumericLogic;
import de.quinscape.domainql.logicimpl.BinaryDataLogic;
import de.quinscape.domainql.logicimpl.CustomParameterProviderLogic;
import de.quinscape.domainql.logicimpl.DegenerifyDBLogic;
import de.quinscape.domainql.logicimpl.DegenerifiedContainerLogic;
import de.quinscape.domainql.logicimpl.DegenerifiedInputLogic;
import de.quinscape.domainql.logicimpl.DegenerifyAndRenameLogic;
import de.quinscape.domainql.logicimpl.DegenerifyContainerLogic;
import de.quinscape.domainql.logicimpl.DegenerifyLogic;
import de.quinscape.domainql.logicimpl.DoubleDegenerificationLogic;
import de.quinscape.domainql.logicimpl.GenericDomainLogic;
import de.quinscape.domainql.logicimpl.GenericDomainOutputLogic;
import de.quinscape.domainql.logicimpl.IgnoredPropsLogic;
import de.quinscape.domainql.logicimpl.ImplicitOverrideLogic;
import de.quinscape.domainql.logicimpl.ImplicitOverrideNonInputLogic;
import de.quinscape.domainql.logicimpl.ListReturningLogic;
import de.quinscape.domainql.logicimpl.LogicWithAnnotated;
import de.quinscape.domainql.logicimpl.LogicWithEnums;
import de.quinscape.domainql.logicimpl.LogicWithEnums2;
import de.quinscape.domainql.logicimpl.LogicWithGenerics;
import de.quinscape.domainql.logicimpl.LogicWithMirrorInput;
import de.quinscape.domainql.logicimpl.LogicWithWrongInjection;
import de.quinscape.domainql.logicimpl.LogicWithWrongInjection2;
import de.quinscape.domainql.logicimpl.MinimalLogic;
import de.quinscape.domainql.logicimpl.NoMirrorLogic;
import de.quinscape.domainql.logicimpl.OutputTypeOverrideByParamLogic;
import de.quinscape.domainql.logicimpl.SumPerMonthLogic;
import de.quinscape.domainql.logicimpl.TestLogic;
import de.quinscape.domainql.logicimpl.OutputTypeOverrideLogic;
import de.quinscape.domainql.logicimpl.TypeParamLogic;
import de.quinscape.domainql.logicimpl.TypeParamMutationLogic;
import de.quinscape.domainql.logicimpl.TypeParamWithNamePatternLogic;
import de.quinscape.domainql.logicimpl.TypeRepeatLogic;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.scalar.BigDecimalScalar;
import de.quinscape.domainql.scalar.BigIntegerScalar;
import de.quinscape.domainql.testdomain.Public;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaPrinter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.quinscape.domainql.testdomain.Tables.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class AnotherDomainQLTest
{

    private final static Logger log = LoggerFactory.getLogger(AnotherDomainQLTest.class);


    @Test
    public void testRelationRenaming()
    {
        // same config as before, but with configured names for the sides that are active

        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TestLogic()))

            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_TWO.TARGET_ID)
                    .withSourceField(SourceField.SCALAR)
                    .withLeftSideObjectName("scalarFieldId")
            )
            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_THREE.TARGET_ID)
                    .withSourceField(SourceField.OBJECT)
                    .withLeftSideObjectName("objField")
            )
            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_FIVE.TARGET_ID)
                    .withTargetField(TargetField.ONE)
                    .withRightSideObjectName("oneObj")
            )
            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_SIX.TARGET_ID)
                    .withTargetField(TargetField.MANY)
                    .withRightSideObjectName("manyObj")
            )
            .buildGraphQLSchema();


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
            assertThat(sourceThree.getFieldDefinition("objField").getType().toString(),is("TargetThree!"));
        }

        // TargetField.ONE
        {
            // just the id fields
            final GraphQLObjectType targetFive = (GraphQLObjectType) schema.getType("TargetFive");
            assertThat(targetFive,is(notNullValue()));
            assertThat(targetFive.getFieldDefinitions().size(),is(2));
            assertThat(targetFive.getFieldDefinition("oneObj").getType().toString(),is("SourceFive!"));
        }

        // TargetField.MANY
        {
            // just the id fields
            final GraphQLObjectType targetSix = (GraphQLObjectType) schema.getType("TargetSix");
            assertThat(targetSix,is(notNullValue()));
            assertThat(targetSix.getFieldDefinitions().size(),is(2));
            assertThat(targetSix.getFieldDefinition("manyObj").getType().toString(),is("[SourceSix]!"));
        }
    }


    @Test
    public void testInputMirrorCreation()
    {

        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Arrays.asList(new TestLogic(), new LogicWithMirrorInput(), new NoMirrorLogic()))
            // test override
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
            .buildGraphQLSchema();


        {

            final GraphQLInputObjectType sourceOneInput = (GraphQLInputObjectType) schema.getType("SourceOneInput");
            assertThat(sourceOneInput, is(notNullValue()));

            assertThat(sourceOneInput.getFields().size(), is(2));
            assertThat(sourceOneInput.getField(DomainObject.ID).getType().toString(),is("String!"));
            assertThat(sourceOneInput.getField("targetId").getType().toString(),is("String!"));
        }

// Type not used, so it is now not generated
//        {
//            final GraphQLInputObjectType sourceOneInput = (GraphQLInputObjectType) schema.getType("SourceTwoInput");
//            assertThat(sourceOneInput, is(notNullValue()));
//
//            assertThat(sourceOneInput.getFields().size(), is(3));
//            assertThat(sourceOneInput.getField(DomainObject.ID).getType(),is(nonNull(Scalars.GraphQLString)));
//            assertThat(sourceOneInput.getField("targetId").getType(),is(nonNull(Scalars.GraphQLString)));
//            assertThat(sourceOneInput.getField("foo").getType(),is(Scalars.GraphQLString));
//        }


        {
            final GraphQLObjectType queryType = (GraphQLObjectType) schema.getType("QueryType");
            final GraphQLFieldDefinition fieldDef = queryType.getFieldDefinition("queryWithMirrorInput");

            final GraphQLArgument arg = fieldDef.getArgument("inputOne");
            assertThat(arg, is(notNullValue()));
            assertThat(((GraphQLNamedType)arg.getType()).getName(), is("SourceOneInput"));
        }

        {
            final GraphQLObjectType queryType = (GraphQLObjectType) schema.getType("NoMirrorInput");
            assertThat(queryType, is(nullValue()));
        }

    }


    @Test(expected = DomainQLTypeException.class)
    public void testFieldConflict()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TestLogic()))
            //.configureRelation( SOURCE_FOUR.TARGET_ID, SourceField.NONE, TargetField.ONE)
            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_FOUR.TARGET_ID)
                    .withTargetField(TargetField.ONE)
            )
            //.configureRelation(SOURCE_FOUR.TARGET2_ID, SourceField.NONE, TargetField.ONE)
            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_FOUR.TARGET2_ID)
                    .withTargetField(TargetField.ONE)
            )
            .buildGraphQLSchema();

        GraphQLObjectType type = (GraphQLObjectType) schema.getType("SourceFour");

        //log.info(type.toString());

    }

    @Test
    public void testFieldConflictResolution()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TestLogic()))
            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_FOUR.TARGET_ID)
                    .withTargetField(TargetField.ONE)
            )
            //.configureRelation(SOURCE_FOUR.TARGET2_ID, SourceField.NONE, TargetField.ONE)
            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_FOUR.TARGET2_ID)
                    .withTargetField(TargetField.ONE)
                    .withRightSideObjectName("source2")
            )
            .buildGraphQLSchema();

    }



    @Test
    public void testObjectAndScalarSourceFields()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TestLogic()))

            // source variants
            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_ONE.TARGET_ID)
                    .withSourceField(SourceField.OBJECT_AND_SCALAR)
            )
            .buildGraphQLSchema();

        // SourceField.OBJECT_AND_SCALAR / TargetField.NONE
        {
            // just the id fields
            final GraphQLObjectType sourceOne = (GraphQLObjectType) schema.getType("SourceOne");
            assertThat(sourceOne,is(notNullValue()));
            assertThat(sourceOne.getFieldDefinitions().size(),is(3));
            assertThat(sourceOne.getFieldDefinitions().get(0).getName(),is(DomainObject.ID));
            assertThat(sourceOne.getFieldDefinitions().get(0).getType().toString(),is("String!"));
            assertThat(sourceOne.getFieldDefinitions().get(1).getName(),is("targetId"));
            assertThat(sourceOne.getFieldDefinitions().get(1).getType().toString(),is("String!"));
            assertThat(sourceOne.getFieldDefinitions().get(2).getName(),is("target"));
            assertThat(sourceOne.getFieldDefinitions().get(2).getType().toString(),is("TargetOne!"));

        }
    }

    @Test
    public void testRenamedObjectAndScalarSourceFields()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TestLogic()))

            // source variants
            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_ONE.TARGET_ID)
                    .withSourceField(SourceField.OBJECT_AND_SCALAR)
                    .withLeftSideObjectName("myTarget")
            )

            .buildGraphQLSchema();

        // SourceField.OBJECT_AND_SCALAR / TargetField.NONE
        {
            // just the id fields
            final GraphQLObjectType sourceOne = (GraphQLObjectType) schema.getType("SourceOne");
            assertThat(sourceOne,is(notNullValue()));
            assertThat(sourceOne.getFieldDefinitions().size(),is(3));
            assertThat(sourceOne.getFieldDefinitions().get(0).getName(),is(DomainObject.ID));
            assertThat(sourceOne.getFieldDefinitions().get(0).getType().toString(),is("String!"));
            assertThat(sourceOne.getFieldDefinitions().get(1).getName(),is("targetId"));
            assertThat(sourceOne.getFieldDefinitions().get(1).getType().toString(),is("String!"));
            assertThat(sourceOne.getFieldDefinitions().get(2).getName(),is("myTarget"));
            assertThat(sourceOne.getFieldDefinitions().get(2).getType().toString(),is("TargetOne!"));

        }
    }

    @Test(expected =  DomainQLTypeException.class)
    public void testWrongTypeAsQueryInput()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new LogicWithWrongInjection()))

            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_ONE.TARGET_ID)
                    .withSourceField(SourceField.OBJECT_AND_SCALAR)
            )
            .buildGraphQLSchema();
    }

    @Test(expected =  DomainQLTypeException.class)
    public void testRecordAsQueryInput()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new LogicWithWrongInjection2()))

            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_ONE.TARGET_ID)
                    .withSourceField(SourceField.OBJECT_AND_SCALAR)
            )
            .buildGraphQLSchema();
    }

    @Test
    public void testAnnotatedFields()
    {
        final DomainQLBuilder builder = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new LogicWithAnnotated()));

        final GraphQLSchema schema = builder.buildGraphQLSchema();

        final GraphQLObjectType outputType = (GraphQLObjectType) schema.getType("AnnotatedBean");
        assertThat(outputType, is(notNullValue()));
        assertThat(((GraphQLNamedType)outputType.getFieldDefinition("value").getType()).getName(), is("Currency"));

        final GraphQLInputObjectType inputType = (GraphQLInputObjectType) schema.getType("AnnotatedBeanInput");
        assertThat(inputType, is(notNullValue()));
        assertThat(((GraphQLNamedType)outputType.getFieldDefinition("value").getType()).getName(), is("Currency"));
    }


    @Test
    public void testTypeRepeat()
    {

        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TypeRepeatLogic()))

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
            .buildGraphQLSchema();
    }
    
    @Test(expected = DomainQLTypeException.class)
    public void testPojoAndObjNameConflict()
    {

        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TypeRepeatLogic()))

            // source variants
            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_SEVEN.TARGET)
                    .withSourceField(SourceField.OBJECT_AND_SCALAR)
            )
            .buildGraphQLSchema();
    }

    @Test(expected = DomainQLTypeException.class)
    public void testPojoAndBackObjNameConflict()
    {

        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(SOURCE_SEVEN, TARGET_SEVEN)
            .logicBeans(Collections.singleton(new TypeRepeatLogic()))

            // source variants
            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_SEVEN.TARGET)
                    .withSourceField(SourceField.OBJECT_AND_SCALAR)
            )
            .buildGraphQLSchema();

        log.info(new SchemaPrinter().print(schema));

    }


    @Test
    public void testCustomParameterProvider()
    {

        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .parameterProvider(new TestParameterProviderFactory())
            .logicBeans(Collections.singleton(new CustomParameterProviderLogic()))
            .buildGraphQLSchema();


        assertThat(schema.getType("DependencyBeanInput"), is(nullValue()));

    }


    @Test
    public void testLogicWithGenerics()
    {

        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new LogicWithGenerics()))
            .buildGraphQLSchema();


        final GraphQLObjectType mutationType = schema.getMutationType();
        {


            final GraphQLFieldDefinition mutation = mutationType.getFieldDefinition(
                "mutationReturningListOfInts");

            assertThat(mutation,is(notNullValue()));

            final GraphQLList type = (GraphQLList) mutation.getType();
            assertThat(((GraphQLNamedType)type.getWrappedType()).getName(), is( "Int"));
        }
        {

            final GraphQLFieldDefinition mutation = mutationType.getFieldDefinition(
                "mutationWithIntListParam");

            assertThat(mutation,is(notNullValue()));

            final GraphQLArgument graphQLArgument = mutation.getArguments().get(0);
            final GraphQLList type = (GraphQLList) graphQLArgument.getType();
            assertThat(((GraphQLNamedType)type.getWrappedType()).getName(), is( "Int"));
        }

        {


            final GraphQLFieldDefinition mutation = mutationType.getFieldDefinition(
                "mutationReturningListOfObject");

            assertThat(mutation,is(notNullValue()));

            final GraphQLList type = (GraphQLList) mutation.getType();
            assertThat(((GraphQLNamedType)type.getWrappedType()).getName(), is( "DependencyBean"));
        }


        {

            final GraphQLFieldDefinition mutation = mutationType.getFieldDefinition(
                "mutationWithObjectListParam");

            assertThat(mutation,is(notNullValue()));

            final GraphQLArgument graphQLArgument = mutation.getArguments().get(0);
            final GraphQLList type = (GraphQLList) graphQLArgument.getType();
            assertThat(((GraphQLNamedType)type.getWrappedType()).getName(), is( "DependencyBeanInput"));
        }

    }


    @Test
    public void testLogicWithEnums()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new LogicWithEnums()))
            .buildGraphQLSchema();
        final GraphQLObjectType queryType = schema.getQueryType();

        {

            GraphQLFieldDefinition query = queryType.getFieldDefinition("queryWithEnumArg");

            assertThat(((GraphQLNamedType)query.getArguments().get(0).getType()).getName(), is("MyEnum"));

            final GraphQLEnumType myEnum = (GraphQLEnumType) schema.getType("MyEnum");
            assertThat(myEnum.getValues().get(0).getName(), is("A"));
            assertThat(myEnum.getValues().get(1).getName(), is("B"));
            assertThat(myEnum.getValues().get(2).getName(), is("C"));
        }

        {

            GraphQLFieldDefinition query = queryType.getFieldDefinition("queryWithObjectArgWithEnum");

            assertThat(((GraphQLNamedType)query.getArguments().get(0).getType()).getName(), is("BeanWithEnumInput"));
            final GraphQLInputObjectType bean = (GraphQLInputObjectType) schema.getType("BeanWithEnumInput");

            assertThat(((GraphQLNamedType)bean.getField("anotherEnum").getType()).getName(), is("AnotherEnum"));

            final GraphQLEnumType myEnum = (GraphQLEnumType) schema.getType("AnotherEnum");
            assertThat(myEnum.getValues().get(0).getName(), is("X"));
            assertThat(myEnum.getValues().get(1).getName(), is("Y"));
            assertThat(myEnum.getValues().get(2).getName(), is("Z"));

        }
    }

    @Test
    public void testLogicWithEnums2()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new LogicWithEnums2()))
            .buildGraphQLSchema();
        final GraphQLObjectType mutationType = schema.getMutationType();
        {

            GraphQLFieldDefinition query = mutationType.getFieldDefinition("enumMutation");

            assertThat(((GraphQLNamedType)query.getType()).getName(), is("MyEnum"));
            final GraphQLEnumType myEnum = (GraphQLEnumType) schema.getType("MyEnum");
            assertThat(myEnum.getValues().get(0).getName(), is("A"));
            assertThat(myEnum.getValues().get(1).getName(), is("B"));
            assertThat(myEnum.getValues().get(2).getName(), is("C"));
        }

        {

            GraphQLFieldDefinition query = mutationType.getFieldDefinition("objectWithEnumMutation");

            assertThat(((GraphQLNamedType)query.getType()).getName(), is("BeanWithEnum"));

            final GraphQLObjectType bean = (GraphQLObjectType) schema.getType("BeanWithEnum");

            assertThat(((GraphQLNamedType)bean.getFieldDefinition("anotherEnum").getType()).getName(), is("AnotherEnum"));

            final GraphQLEnumType myEnum = (GraphQLEnumType) schema.getType("AnotherEnum");
            assertThat(myEnum.getValues().get(0).getName(), is("X"));
            assertThat(myEnum.getValues().get(1).getName(), is("Y"));
            assertThat(myEnum.getValues().get(2).getName(), is("Z"));
        }
    }

    @Test
    public void testDegenerify()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new DegenerifyLogic()))
            .buildGraphQLSchema();

        final GraphQLObjectType pagedPayload = (GraphQLObjectType) schema.getType("PagedPayload");

        assertThat(pagedPayload, is(notNullValue()));
        assertThat(pagedPayload.getFieldDefinitions().size(), is(2));
        final GraphQLList listProp = (GraphQLList) ((GraphQLNonNull)pagedPayload.getFieldDefinitions().get(0).getType()).getWrappedType();
        final GraphQLOutputType numProp = (GraphQLOutputType) ((GraphQLNonNull)pagedPayload.getFieldDefinitions().get(1).getType()).getWrappedType();
        assertThat(((GraphQLNamedType)listProp.getWrappedType()).getName(), is( "Payload"));
        assertThat(((GraphQLNamedType)numProp).getName(), is( "Int"));


        assertThat(schema.getType("Paged"), is(nullValue()));

        final GraphQLObjectType queryType = schema.getQueryType();
        GraphQLFieldDefinition query = queryType.getFieldDefinition("getPayload");

        assertThat(query,is(notNullValue()));
        assertThat(((GraphQLNamedType)query.getType()).getName(), is("PagedPayload"));

    }

    @Test
    public void testDegenerifyRename()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new DegenerifyAndRenameLogic()))
            .buildGraphQLSchema();

        final GraphQLObjectType pagedPayload = (GraphQLObjectType) schema.getType("PagedAndRenamed");

        assertThat(pagedPayload, is(notNullValue()));
        assertThat(pagedPayload.getFieldDefinitions().size(), is(2));
        final GraphQLList listProp = (GraphQLList) ((GraphQLNonNull)pagedPayload.getFieldDefinitions().get(0).getType()).getWrappedType();
        final GraphQLOutputType numProp = (GraphQLOutputType) ((GraphQLNonNull)pagedPayload.getFieldDefinitions().get(1).getType()).getWrappedType();
        assertThat(((GraphQLNamedType)listProp.getWrappedType()).getName(), is( "AnnotatedPayload"));
        assertThat(((GraphQLNamedType)numProp).getName(), is( "Int"));


        assertThat(schema.getType("Paged"), is(nullValue()));

        final GraphQLObjectType queryType = schema.getQueryType();
        GraphQLFieldDefinition query = queryType.getFieldDefinition("getPayload");

        assertThat(query,is(notNullValue()));
        assertThat(((GraphQLNamedType)query.getType()).getName(), is("PagedAndRenamed"));

    }

    @Test
    public void testDegenerifiedDBObject()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new DegenerifyDBLogic()))
            .buildGraphQLSchema();

//        for (Map.Entry<String, GraphQLType> e : schema.getTypeMap().entrySet())
//        {
//            log.info("{}.: {}", e.getKey(), e.getValue());
//        }

        final GraphQLObjectType pagedPayload = (GraphQLObjectType) schema.getType("PagedSourceOne");

        assertThat(pagedPayload, is(notNullValue()));
        assertThat(pagedPayload.getFieldDefinitions().size(), is(2));
        final GraphQLList listProp = (GraphQLList) ((GraphQLNonNull)pagedPayload.getFieldDefinitions().get(0).getType()).getWrappedType();
        final GraphQLOutputType numProp = (GraphQLOutputType) ((GraphQLNonNull)pagedPayload.getFieldDefinitions().get(1).getType()).getWrappedType();
        assertThat(((GraphQLNamedType)listProp.getWrappedType()).getName(), is( "SourceOne"));
        assertThat(((GraphQLNamedType)numProp).getName(), is( "Int"));


        assertThat(schema.getType("Paged"), is(nullValue()));

        final GraphQLObjectType queryType = schema.getQueryType();
        GraphQLFieldDefinition query = queryType.getFieldDefinition("sourceOnes");

        assertThat(query,is(notNullValue()));
        assertThat(((GraphQLNamedType)query.getType()).getName(), is("PagedSourceOne"));

    }


    @Test
    public void testListReturningLogic()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new ListReturningLogic()))
            .buildGraphQLSchema();

        final GraphQLObjectType queryType = schema.getQueryType();

        GraphQLFieldDefinition query = queryType.getFieldDefinition("listOfThrees");
        assertThat(query,is(notNullValue()));
        final GraphQLList type = (GraphQLList) query.getType();
        assertThat(((GraphQLNamedType)type.getWrappedType()).getName(), is("SourceThree"));
    }


    @Test
    public void testImplicitOverride()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new ImplicitOverrideLogic()))
            .buildGraphQLSchema();

        final GraphQLObjectType queryType = schema.getQueryType();

        GraphQLFieldDefinition query = queryType.getFieldDefinition("queryThatOverrides");
        assertThat(query,is(notNullValue()));
        final GraphQLArgument arg = query.getArgument("sourceOneInput");
        assertThat(arg,is(notNullValue()));

        assertThat(((GraphQLNamedType)arg.getType()).getName(), is("SourceOneInput"));

        final GraphQLInputObjectType sourceOneInput = (GraphQLInputObjectType) schema.getType("SourceOneInput");
        assertThat(sourceOneInput.getField(DomainObject.ID).getType().toString(),is("String!"));
        assertThat(sourceOneInput.getField("targetId").getType().toString(),is("String!"));
        assertThat(sourceOneInput.getField("extra").getType(),is(Scalars.GraphQLString));
    }

    @Test
    public void testImplicitOverrideNonInputLogic()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new ImplicitOverrideNonInputLogic()))
            .buildGraphQLSchema();

        final GraphQLObjectType queryType = schema.getQueryType();

        GraphQLFieldDefinition query = queryType.getFieldDefinition("queryThatOverrides");
        assertThat(query,is(notNullValue()));
        final GraphQLArgument arg = query.getArgument("sourceOneInput");
        assertThat(arg,is(notNullValue()));

        assertThat(((GraphQLNamedType)arg.getType()).getName(), is("SourceOneInput"));

        final GraphQLInputObjectType sourceOneInput = (GraphQLInputObjectType) schema.getType("SourceOneInput");
        assertThat(sourceOneInput.getField(DomainObject.ID).getType().toString(),is("String!"));
        assertThat(sourceOneInput.getField("targetId").getType().toString(),is("String!"));
        assertThat(sourceOneInput.getField("extra").getType(),is(Scalars.GraphQLString));

    }


    @Test
    public void testDegenerifiedListInput()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new DegenerifiedInputLogic()))
            .buildGraphQLSchema();


        final GraphQLInputObjectType sourceOneInput = (GraphQLInputObjectType) schema.getType("PagedPayloadInput");
        assertThat(sourceOneInput.getField("rowCount").getType().toString(),is("Int!"));
        assertThat(sourceOneInput.getField("rows").getType().toString(),is("[PayloadInput]!"));


    }

    @Test
    public void testDegenerifiedInput()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new DegenerifiedContainerLogic()))
            .buildGraphQLSchema();

        final GraphQLInputObjectType sourceOneInput = (GraphQLInputObjectType) schema.getType("ContainerPayloadInput");
        assertThat(sourceOneInput.getField("num").getType().toString(),is("Int!"));
        assertThat(sourceOneInput.getField("value").getType(),is(schema.getType("PayloadInput")));

    }


    @Test
    public void testDegenerifiedContainerOutput()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new DegenerifyContainerLogic()))
            .buildGraphQLSchema();

        final GraphQLObjectType containerPayload = (GraphQLObjectType) schema.getType("ContainerPayload");

        //log.info(containerPayload.getFieldDefinitions().toString());

        assertThat(containerPayload.getFieldDefinition("num").getType().toString(),is("Int!"));
        assertThat(containerPayload.getFieldDefinition("value").getType(),is(schema.getType("Payload")));

    }


    @Test
    public void testDoubleDegenerification()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .logicBeans(Collections.singleton(new DoubleDegenerificationLogic()))
            .buildGraphQLSchema();


    }


    @Test
    public void testGenericDomainObject()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new GenericDomainLogic()))
            .withAdditionalScalar(DomainObject.class, DomainObjectScalar.newDomainObjectScalar())
            .buildGraphQLSchema();

    }


    @Test(expected = DomainQLException.class)
    public void testGenericDomainObjectWithoutScalar()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new GenericDomainLogic()))
            .buildGraphQLSchema();

    }

    @Test
    public void testGenericDomainObjectOutput()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .withAdditionalScalar(DomainObject.class, DomainObjectScalar.newDomainObjectScalar())
            .logicBeans(Collections.singleton(new GenericDomainOutputLogic()))
            .buildGraphQLSchema();

        final GraphQLObjectType queryType = (GraphQLObjectType) schema.getType("QueryType");
        final GraphQLFieldDefinition fieldDef = queryType.getFieldDefinition("queryDomainObject");

        assertThat(fieldDef, is(notNullValue()));
        assertThat(fieldDef.getType() instanceof GraphQLScalarType, is(true));
        assertThat(((GraphQLNamedType)fieldDef.getType()).getName(), is("DomainObject"));
    }

    @Test
    public void testFieldLookup()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new MinimalLogic()))
            .build();
        final GraphQLSchema schema = domainQL
            .getGraphQLSchema();

        //log.info(domainQL.getFieldLookup().toString());

        assertThat(domainQL.lookupField("SourceFour", DomainObject.ID).getName(), is("id"));
        assertThat(domainQL.lookupField("SourceFour", "targetId").getName(), is("target_id"));

    }

    @Test
    public void testTypeParameters()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TypeParamLogic()))
            .build();
        final GraphQLSchema schema = domainQL
            .getGraphQLSchema();

        //log.info(domainQL.getFieldLookup().toString());

        final GraphQLObjectType queryType = schema.getQueryType();

        GraphQLFieldDefinition queryA = queryType.getFieldDefinition("queryTypeA");
        assertThat(queryA,is(notNullValue()));
        assertThat(((GraphQLNamedType)queryA.getType()).getName(), is("TypeA"));

        GraphQLFieldDefinition queryB = queryType.getFieldDefinition("queryTypeB");
        assertThat(queryB,is(notNullValue()));
        assertThat(((GraphQLNamedType)queryB.getType()).getName(), is("TypeB"));

        GraphQLFieldDefinition queryContainerA = queryType.getFieldDefinition("queryContainerTypeA");
        assertThat(queryContainerA,is(notNullValue()));
        assertThat(((GraphQLNamedType)queryContainerA.getType()).getName(), is("ContainerTypeA"));

        GraphQLFieldDefinition queryContainerB = queryType.getFieldDefinition("queryContainerTypeB");
        assertThat(queryContainerB,is(notNullValue()));
        assertThat(((GraphQLNamedType)queryContainerB.getType()).getName(), is("ContainerTypeB"));

        final GraphQLObjectType type = (GraphQLObjectType) schema.getType(((GraphQLNamedType)queryContainerB.getType()).getName());
        assertThat(type.getDescription(), is("Generated for de.quinscape.domainql.beans.Container<TypeB>"));
        assertThat(type.getFieldDefinitions().size(), is(2));
        assertThat(type.getFieldDefinitions().get(0).getName(), is("value"));
        assertThat(((GraphQLNamedType)type.getFieldDefinitions().get(0).getType()).getName(), is("TypeB"));


        GraphQLFieldDefinition queryListA = queryType.getFieldDefinition("queryListTypeA");
        assertThat(queryListA,is(notNullValue()));
        assertThat(queryListA.getType(),is(instanceOf(GraphQLList.class)));
        assertThat(((GraphQLNamedType)((GraphQLList)queryListA.getType()).getWrappedType()).getName(), is("TypeA"));

        GraphQLFieldDefinition queryListB = queryType.getFieldDefinition("queryListTypeB");
        assertThat(queryListB,is(notNullValue()));
        assertThat(queryListB.getType(),is(instanceOf(GraphQLList.class)));
        assertThat(((GraphQLNamedType)((GraphQLList)queryListB.getType()).getWrappedType()).getName(), is("TypeB"));
    }

    @Test
    public void testTypeParametersForMutations()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TypeParamMutationLogic()))
            .build();
        final GraphQLSchema schema = domainQL
            .getGraphQLSchema();

        //log.info(domainQL.getFieldLookup().toString());

        final GraphQLObjectType mutationType = schema.getMutationType();

        GraphQLFieldDefinition mutationA = mutationType.getFieldDefinition("mutateTypeA");
        assertThat(mutationA,is(notNullValue()));
        assertThat(((GraphQLNamedType)mutationA.getType()).getName(), is("TypeA"));

        GraphQLFieldDefinition mutationB = mutationType.getFieldDefinition("mutateTypeB");
        assertThat(mutationB,is(notNullValue()));
        assertThat(((GraphQLNamedType)mutationB.getType()).getName(), is("TypeB"));

        GraphQLFieldDefinition mutationContainerA = mutationType.getFieldDefinition("mutateContainerTypeA");
        assertThat(mutationContainerA,is(notNullValue()));
        assertThat(((GraphQLNamedType)mutationContainerA.getType()).getName(), is("ContainerTypeA"));

        GraphQLFieldDefinition mutationContainerB = mutationType.getFieldDefinition("mutateContainerTypeB");
        assertThat(mutationContainerB,is(notNullValue()));
        assertThat(((GraphQLNamedType)mutationContainerB.getType()).getName(), is("ContainerTypeB"));

        final GraphQLObjectType type = (GraphQLObjectType) schema.getType(((GraphQLNamedType)mutationContainerB.getType()).getName());
        assertThat(type.getDescription(), is("Generated for de.quinscape.domainql.beans.Container<TypeB>"));
        assertThat(type.getFieldDefinitions().size(), is(2));
        assertThat(type.getFieldDefinitions().get(0).getName(), is("value"));
        assertThat(((GraphQLNamedType)type.getFieldDefinitions().get(0).getType()).getName(), is("TypeB"));


        GraphQLFieldDefinition mutationListA = mutationType.getFieldDefinition("mutateListTypeA");
        assertThat(mutationListA,is(notNullValue()));
        assertThat(mutationListA.getType(),is(instanceOf(GraphQLList.class)));
        assertThat(((GraphQLNamedType)((GraphQLList)mutationListA.getType()).getWrappedType()).getName(), is("TypeA"));

        GraphQLFieldDefinition mutationListB = mutationType.getFieldDefinition("mutateListTypeB");
        assertThat(mutationListB,is(notNullValue()));
        assertThat(mutationListB.getType(),is(instanceOf(GraphQLList.class)));
        assertThat(((GraphQLNamedType)((GraphQLList)mutationListB.getType()).getWrappedType()).getName(), is("TypeB"));
    }

    @Test
    public void testTypeParameterWithPattern()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TypeParamWithNamePatternLogic()))
            .build();
        final GraphQLSchema schema = domainQL
            .getGraphQLSchema();

        //log.info(domainQL.getFieldLookup().toString());

        final GraphQLObjectType queryType = schema.getQueryType();

        GraphQLFieldDefinition queryA = queryType.getFieldDefinition("TypeAQuery");
        assertThat(queryA,is(notNullValue()));
        assertThat(((GraphQLNamedType)queryA.getType()).getName(), is("TypeA"));

        GraphQLFieldDefinition queryB = queryType.getFieldDefinition("TypeBQuery");
        assertThat(queryB,is(notNullValue()));
        assertThat(((GraphQLNamedType)queryB.getType()).getName(), is("TypeB"));
    }


    @Test
    public void testIgnoredProps()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new IgnoredPropsLogic()))
            .build();
        final GraphQLSchema schema = domainQL
            .getGraphQLSchema();

        //log.info(domainQL.getFieldLookup().toString());

        final GraphQLObjectType queryType = schema.getQueryType();

        GraphQLFieldDefinition queryB = queryType.getFieldDefinition("igPropBean");
        assertThat(queryB,is(notNullValue()));

        final GraphQLObjectType type = (GraphQLObjectType) schema.getType("IgnoredPropsBean");
        final List<GraphQLFieldDefinition> fieldDefinitions = type.getFieldDefinitions();
        assertThat(fieldDefinitions.size(), is(1));
        assertThat(fieldDefinitions.get(0).getName(), is("value"));

    }


    @Test
    public void testGenericScalar()
    {
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .withAdditionalScalar(GenericScalar.class, GenericScalarType.newGenericScalar())
            .withAdditionalScalar(DomainObject.class, DomainObjectScalar.newDomainObjectScalar())
            .logicBeans(Collections.singleton(new GenericScalarLogic()))
            .buildGraphQLSchema();

        final GraphQLObjectType queryType = (GraphQLObjectType) schema.getType("MutationType");
        final GraphQLFieldDefinition fieldDef = queryType.getFieldDefinition("genericScalarLogic");

        final GraphQLArgument arg = fieldDef.getArgument("value");
        assertThat(arg, is(notNullValue()));
        assertThat(((GraphQLNamedType)arg.getType()).getName(), is("GenericScalar"));

        assertThat(fieldDef, is(notNullValue()));
        assertThat(fieldDef.getType() instanceof GraphQLScalarType, is(true));
        assertThat(((GraphQLNamedType)fieldDef.getType()).getName(), is("GenericScalar"));
    }



    @Test
    public void testDBView()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new SumPerMonthLogic()))
            .objectType(SumPerMonth.class)
            .build();

        assertThat(
            domainQL.getJooqTable("SumPerMonth").getName(), is("sum_per_month")
        );
        assertThat(
            domainQL.lookupField("SumPerMonth", "month").getName(), is("month")
        );

        final GraphQLSchema schema = domainQL.getGraphQLSchema();
        final GraphQLObjectType objType = (GraphQLObjectType) schema.getType("SumPerMonth");

        final GraphQLOutputType monthFieldType = objType.getFieldDefinition("month").getType();
        assertThat(GraphQLTypeUtil.isNonNull(monthFieldType), is(true));

        final GraphQLOutputType sumFieldType = objType.getFieldDefinition("sum").getType();
        assertThat(GraphQLTypeUtil.isNonNull(sumFieldType), is(false));

        final GraphQLObjectType queryType = (GraphQLObjectType) schema.getType("QueryType");
        final GraphQLFieldDefinition fieldDef = queryType.getFieldDefinition("getSumPerMonthLogic");

        assertThat(fieldDef,is(notNullValue()));
        assertThat(((GraphQLNamedType)fieldDef.getType()).getName(),is("SumPerMonth"));
        assertThat(fieldDef.getArguments().size(),is(1));

        assertThat(fieldDef.getArguments().get(0).getType().toString(),is("Int!"));


        //log.info(new SchemaPrinter().print(schema));

    }

    @Test
    public void testBinaryData()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new BinaryDataLogic()))
            .build();

        //log.info(new SchemaPrinter().print(domainQL.getGraphQLSchema()));

        final GraphQLObjectType binaryBean = (GraphQLObjectType) domainQL.getGraphQLSchema().getType("BinaryBean");

        final GraphQLOutputType type = binaryBean.getFieldDefinition("data").getType();
        assertThat(type, is( instanceOf(GraphQLList.class)));
        assertThat(GraphQLTypeUtil.unwrapAll(type).getName(), is( "Byte"));

    }

    @Test
    public void testBigNumericTypes()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new BigNumericLogic()))
            .withAdditionalScalar(BigDecimal.class, new BigDecimalScalar())
            .withAdditionalScalar(BigInteger.class, new BigIntegerScalar())
            .build();

        //log.info(new SchemaPrinter().print(domainQL.getGraphQLSchema()));

        final GraphQLObjectType bdContainerType = (GraphQLObjectType) domainQL.getGraphQLSchema().getType("BDContainer");
        final GraphQLOutputType bdValueType = bdContainerType.getFieldDefinition("value").getType();
        assertThat(((GraphQLNamedType)bdValueType).getName(), is( "BigDecimal"));

        final GraphQLObjectType biContainerType = (GraphQLObjectType) domainQL.getGraphQLSchema().getType("BIContainer");
        final GraphQLOutputType biValueType = biContainerType.getFieldDefinition("value").getType();
        assertThat(((GraphQLNamedType)biValueType).getName(), is( "BigInteger"));
    }

    @Test
    public void testOutputTypeOverride()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new OutputTypeOverrideLogic()))
            .withRelation(
                new RelationBuilder()
                    .withId("SourceSeven-renamedTarget")
                    .withForeignKeyFields(SOURCE_SEVEN.TARGET)
                    .withSourceField(SourceField.OBJECT)
                    .withTargetField(TargetField.NONE)
                    .withLeftSideObjectName("targetObj")
            )
            .build();
        final GraphQLSchema schema = domainQL.getGraphQLSchema();


        final OutputType ot = domainQL.getTypeRegistry().lookup("TargetSeven");
        assertThat(ot.getJavaType().getName(), is(TargetSeven.class.getName()));

        final GraphQLObjectType gqlType = (GraphQLObjectType) schema.getType("TargetSeven");
        final GraphQLFieldDefinition fieldDef = gqlType.getFieldDefinition("concat");

        final GraphQLDirective computed = fieldDef.getDirective("computed");
        assertThat(computed, is(notNullValue()));

        final GraphQLScalarType scalarType = (GraphQLScalarType) fieldDef.getType();
        assertThat(scalarType.getName(), is("String"));
        assertThat(gqlType.getFieldDefinitions().size(), is(3));

        final GraphQLInputObjectType inputType = (GraphQLInputObjectType) schema.getType("TargetSevenInput");
        final GraphQLInputObjectField concat = inputType.getFieldDefinition("concat");
        assertThat(concat, is( nullValue()));

        assertThat(domainQL.getMetaData().getRelationModels().get(0).getSourcePojoClass().getName(), is(SourceSeven.class.getName()));
        assertThat(domainQL.getMetaData().getRelationModels().get(0).getTargetPojoClass().getName(), is(TargetSeven.class.getName()));

        assertThat(domainQL.lookupType("SourceSeven").getPojoType().getName(), is(SourceSeven.class.getName()));
        assertThat(domainQL.lookupType("TargetSeven").getPojoType().getName(), is(TargetSeven.class.getName()));
    }

    @Test
    public void testOutputTypeOverrideByParam()
    {
        // checks that output type overriding works via @GraphQLTypeParam, too
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new OutputTypeOverrideByParamLogic()))
            .withRelation(
                new RelationBuilder()
                    .withId("SourceSeven-renamedTarget")
                    .withForeignKeyFields(SOURCE_SEVEN.TARGET)
                    .withSourceField(SourceField.OBJECT)
                    .withTargetField(TargetField.NONE)
                    .withLeftSideObjectName("targetObj")
            )
            .build();
        final GraphQLSchema schema = domainQL.getGraphQLSchema();


        final OutputType ot = domainQL.getTypeRegistry().lookup("TargetSeven");
        assertThat(ot.getJavaType().getName(), is(TargetSeven.class.getName()));

        final GraphQLObjectType gqlType = (GraphQLObjectType) schema.getType("TargetSeven");
        final GraphQLFieldDefinition fieldDef = gqlType.getFieldDefinition("concat");

        final GraphQLDirective computed = fieldDef.getDirective("computed");
        assertThat(computed, is(notNullValue()));

        final GraphQLScalarType scalarType = (GraphQLScalarType) fieldDef.getType();
        assertThat(scalarType.getName(), is("String"));
        assertThat(gqlType.getFieldDefinitions().size(), is(3));

    }
}

