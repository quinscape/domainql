package de.quinscape.domainql.util;

import com.google.common.collect.ImmutableMap;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class JSONHolderTest
{
    @Test
    public void testJSONHolder()
    {
        {
            final String json = jsonifyMap(new JSONHolder("\"blabla\""));
            assertThat(json,is("{\"value\":\"blabla\"}"));
        }
        {
            final String json = jsonifyMap(new JSONHolder(ImmutableMap.of("foo", 1)));
            assertThat(json,is("{\"value\":{\"foo\":1}}"));
        }
    }


    private String jsonifyMap(JSONHolder holder)
    {
        final ImmutableMap<String, JSONHolder> map = ImmutableMap.of("value", holder);
        return JSONUtil.DEFAULT_GENERATOR.forValue(map);
    }


}
