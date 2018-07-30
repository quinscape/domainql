package de.quinscape.domainql;

import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class TestFetcher
    implements DataFetcher<String>
{

    private final String name;

    private final String data;


    public TestFetcher(String name, String data)
    {

        this.name = name;
        this.data = data;
    }

    @Override
    public String get(DataFetchingEnvironment environment)
    {
        final Object value = JSONUtil.DEFAULT_UTIL.getProperty(environment.getSource(), name);
        return data + ":" + value;
    }
}
