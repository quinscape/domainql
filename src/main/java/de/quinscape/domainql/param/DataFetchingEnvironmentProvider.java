package de.quinscape.domainql.param;

import graphql.schema.DataFetchingEnvironment;

public class DataFetchingEnvironmentProvider
    implements ParameterProvider<DataFetchingEnvironment>
{
    public final static DataFetchingEnvironmentProvider INSTANCE = new DataFetchingEnvironmentProvider();

    private DataFetchingEnvironmentProvider()
    {

    }

    @Override
    public DataFetchingEnvironment provide(DataFetchingEnvironment environment)
    {
        return environment;
    }
}
