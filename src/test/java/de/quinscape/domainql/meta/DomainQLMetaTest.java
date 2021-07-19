package de.quinscape.domainql.meta;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.DomainQLTypeException;
import de.quinscape.domainql.RelationBuilder;
import de.quinscape.domainql.config.RelationModel;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.logicimpl.ConfigureNonDBByNameLogic;
import de.quinscape.domainql.logicimpl.OutputTypeOverrideByParamLogic;
import de.quinscape.domainql.logicimpl.TestLogic;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.pojos.Bar;
import de.quinscape.domainql.testdomain.tables.pojos.BarOrg;
import de.quinscape.domainql.testdomain.tables.pojos.BarOwner;
import de.quinscape.domainql.testdomain.tables.pojos.Foo;
import de.quinscape.domainql.testdomain.tables.pojos.TargetNine;
import de.quinscape.domainql.testdomain.tables.pojos.TargetNineCounts;
import graphql.schema.GraphQLSchema;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.quinscape.domainql.testdomain.Tables.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DomainQLMetaTest
{
    private final static Logger log = LoggerFactory.getLogger(DomainQLMetaTest.class);


    @Test
    public void testMetadataGeneration()
    {
        // checks that output type overriding works via @GraphQLTypeParam, too
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)

            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_SIX.TARGET_ID)
                    .withTargetField(TargetField.MANY)
                    .withRightSideObjectName("manyObj")
            )


            .configureNameField("name")

            .logicBeans(Collections.singleton(new OutputTypeOverrideByParamLogic()))
            .build();
        final GraphQLSchema schema = domainQL.getGraphQLSchema();

        final List<RelationModel> relations = (List<RelationModel>) domainQL.getMetaData().getData().get("relations");

        assertThat(relations.size(), is(1));

        final RelationModel relationModel = relations.get(0);

        log.info("{}", relationModel);

        //log.info(new SchemaPrinter().print(domainQL.getGraphQLSchema()));

    }

    @Test
    public void testNameFields()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)

            .configureRelation(BAR.OWNER_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)
            .configureRelation(BAR_OWNER.ORG_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)

            .configureNameFieldForTypes("name", BarOwner.class, BarOrg.class, Foo.class)
            .configureNameFields(Bar.class,"name", "owner.name", "owner.org.name")
            .build();


        final DomainQLTypeMeta barMeta = domainQL.getMetaData().getTypeMeta("Bar");
        final DomainQLTypeMeta barOwnerMeta = domainQL.getMetaData().getTypeMeta("BarOwner");
        final DomainQLTypeMeta barOrgMeta = domainQL.getMetaData().getTypeMeta("BarOrg");
        final DomainQLTypeMeta fooMeta = domainQL.getMetaData().getTypeMeta("Foo");

        assertThat( barMeta.getMeta(DomainQLMeta.NAME_FIELDS), is(Arrays.asList("name", "owner.name", "owner.org.name")) );
        assertThat( barOwnerMeta.getMeta(DomainQLMeta.NAME_FIELDS), is(Collections.singletonList("name")) );
        assertThat( barOrgMeta.getMeta(DomainQLMeta.NAME_FIELDS), is(Collections.singletonList("name")) );
        assertThat( fooMeta.getMeta(DomainQLMeta.NAME_FIELDS), is(Collections.singletonList("name")) );
    }

    @Test(expected = DomainQLTypeException.class)
    public void testNamingFieldsManyToMany()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)

            .configureRelation(BAR.OWNER_ID, SourceField.OBJECT_AND_SCALAR, TargetField.MANY)
            .configureRelation(BAR_OWNER.ORG_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)

            // this makes sense from a GraphQL logic point of view, but we don't want the complications
            // and the use-case for this is weak at best
            .configureNameFields(BarOwner.class,"name", "bars.name")
            .build();


    }

    @Test(expected = DomainQLTypeException.class)
    public void testNamingFieldsError()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)

            .configureRelation(BAR.OWNER_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)
            .configureRelation(BAR_OWNER.ORG_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)

            .configureNameFields(Bar.class,"name", "wrong.name")
            .build();
    }

    @Test
    public void testNameFieldConfiguringByName()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)

            .configureRelation(BAR.OWNER_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)
            .configureRelation(BAR_OWNER.ORG_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)

            .configureNameField("name")
            .configureNameFields(Bar.class,"name", "owner.name", "owner.org.name")
            .build();

        final DomainQLTypeMeta barMeta = domainQL.getMetaData().getTypeMeta("Bar");
        final DomainQLTypeMeta barOwnerMeta = domainQL.getMetaData().getTypeMeta("BarOwner");
        final DomainQLTypeMeta barOrgMeta = domainQL.getMetaData().getTypeMeta("BarOrg");
        final DomainQLTypeMeta fooMeta = domainQL.getMetaData().getTypeMeta("Foo");

        assertThat( barMeta.getMeta(DomainQLMeta.NAME_FIELDS), is(Arrays.asList("name", "owner.name", "owner.org.name")) );
        assertThat( barOwnerMeta.getMeta(DomainQLMeta.NAME_FIELDS), is(Collections.singletonList("name")) );
        assertThat( barOrgMeta.getMeta(DomainQLMeta.NAME_FIELDS), is(Collections.singletonList("name")) );
        assertThat( fooMeta.getMeta(DomainQLMeta.NAME_FIELDS), is(Collections.singletonList("name")) );

    }

    @Test
    public void testNameFieldConfiguringNonDBByName()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .logicBeans(new ConfigureNonDBByNameLogic())
            .objectTypes(Public.PUBLIC)

            .configureRelation(BAR.OWNER_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)
            .configureRelation(BAR_OWNER.ORG_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)

            .configureNameField("name")
            .configureNameFields(Bar.class,"name", "owner.name", "owner.org.name")
            .build();


        final DomainQLTypeMeta fullResponseMeta = domainQL.getMetaData().getTypeMeta("FullResponse");

        assertThat( fullResponseMeta.getMeta(DomainQLMeta.NAME_FIELDS), is(Collections.singletonList("name")) );
    }


    @Test
    public void testRelationModelMetadata()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .logicBeans(Collections.singleton(new TestLogic()))

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

        final List<RelationModel> relationModels = (List<RelationModel>) domainQL.getMetaData().getData().get("relations");


        assertThat(relationModels.get(0).getId(), is("SourceTwo-target"));
        assertThat(relationModels.get(1).getId(), is("SourceTwo-target2"));
        assertThat(relationModels.get(5).getId(), is("SourceSeven-renamedTarget"));
    }
}
