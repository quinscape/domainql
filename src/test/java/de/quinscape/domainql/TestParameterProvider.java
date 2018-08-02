package de.quinscape.domainql;

import de.quinscape.domainql.beans.TestParamType;
import de.quinscape.domainql.param.ParameterProvider;
import graphql.schema.DataFetchingEnvironment;

public class TestParameterProvider
    implements ParameterProvider<TestParamType>
{
    @Override
    public TestParamType provide(DataFetchingEnvironment environment)
    {
        return new TestParamType();
    }
}
