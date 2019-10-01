package de.quinscape.domainql.schema;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.RelationBuilder;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.logicimpl.DegenerifyLogic;
import de.quinscape.domainql.util.Paged;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;
import org.svenson.JSONParser;
import org.svenson.util.JSONBeanUtil;
import org.svenson.util.JSONPathUtil;

import java.util.List;
import java.util.Map;

import static de.quinscape.domainql.testdomain.Tables.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class SchemaDataProviderTest
{
    private final static Logger log = LoggerFactory.getLogger(SchemaDataProviderTest.class);

    private final static JSONPathUtil pathUtil = new JSONPathUtil(JSONUtil.OBJECT_SUPPORT);

    private JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;


    @Test
    public void testSchemaProviding() throws Exception
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .logicBeans(new DegenerifyLogic())
            .objectTypes(SOURCE_TWO, TARGET_TWO)
            .withRelation(
                new RelationBuilder()
                    .withForeignKeyFields(SOURCE_TWO.TARGET_ID)
                    .withSourceField(SourceField.SCALAR)
            )
            .build();

        final SchemaDataProvider provider = new SchemaDataProvider(domainQL);

        final TestJsViewContext ctx = new TestJsViewContext();
        provider.provide(ctx);

        final JSON gen = JSONUtil.DEFAULT_GENERATOR;
        final JSONParser parser = JSONUtil.DEFAULT_PARSER;
        final Map<String, Object> schemaData =
            parser.parse(
                Map.class,
                gen.forValue(ctx.getViewData().get("schema")
                )
            );

        //log.info("{}", JSONUtil.DEFAULT_GENERATOR.dumpObjectFormatted(schemaData));

        List<Object> types = (List<Object>) pathUtil.getPropertyPath(schemaData, "types");

        assertThat(types, is(notNullValue()));

        final Object sourceTwoType = findNamed(types, "SourceTwo");

        assertThat(sourceTwoType, is(notNullValue()));
        assertThat(pathUtil.getPropertyPath(sourceTwoType, "fields[0].name"), is(DomainObject.ID));
        assertThat(pathUtil.getPropertyPath(sourceTwoType, "fields[1].name"), is("targetId"));

        final List<String> genericTypes = (List<String>) pathUtil.getPropertyPath(schemaData, "genericTypes");
        assertThat(genericTypes.size(), is(1));
        assertThat(pathUtil.getPropertyPath(schemaData, "genericTypes[0].genericType"), is( Paged.class.getName()));
        assertThat(pathUtil.getPropertyPath(schemaData, "genericTypes[0].type"), is( "PagedPayload"));
        assertThat(pathUtil.getPropertyPath(schemaData, "genericTypes[0].typeParameters[0]"), is( "Payload"));

        final Object stringType = findNamed(types, "String");
        assertThat(stringType, is(notNullValue()));
        assertThat(util.getProperty(stringType, "kind"), is("SCALAR"));

    }


    private Object findNamed(List<Object> types, String name)
    {
        for (Object type : types)
        {
            if (name.equals(util.getProperty(type, "name")))
            {
                return type;
            }
        }
        return null;
    }
}
