package de.quinscape.domainql.schema;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.testdomain.Tables;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.GraphQLSchema;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        final GraphQLSchema schema = DomainQL.newDomainQL(null)
            .objectTypes(Tables.SOURCE_TWO)
            .configureRelation(   SOURCE_TWO.TARGET_ID, SourceField.SCALAR, TargetField.NONE)
            .buildGraphQLSchema();

        final SchemaDataProvider provider = new SchemaDataProvider(schema);

        final TestJsViewContext ctx = new TestJsViewContext();
        provider.provide(ctx);

        final Map<String,Object> schemaData = (Map<String, Object>) ctx.getViewData().get("schema");

        List<Object> types = (List<Object>) pathUtil.getPropertyPath(schemaData, "types");

        assertThat(types, is(notNullValue()));

        final Object sourceTwoType = findNamed(types, "SourceTwo");

        assertThat(sourceTwoType, is(notNullValue()));
        assertThat(pathUtil.getPropertyPath(sourceTwoType, "fields[0].name"), is("id"));
        assertThat(pathUtil.getPropertyPath(sourceTwoType, "fields[1].name"), is("targetId"));

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
