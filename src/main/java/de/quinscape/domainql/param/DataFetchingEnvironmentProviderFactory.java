package de.quinscape.domainql.param;

import graphql.schema.DataFetchingEnvironment;

import java.lang.annotation.Annotation;

public class DataFetchingEnvironmentProviderFactory
    implements ParameterProviderFactory
{
    @Override
    public ParameterProvider createIfApplicable(
        Class<?> parameterClass, Annotation[] annotations
    ) throws Exception
    {
        if (DataFetchingEnvironment.class.isAssignableFrom(parameterClass))
        {
            return DataFetchingEnvironmentProvider.INSTANCE;
        }
        return null;
    }
}
