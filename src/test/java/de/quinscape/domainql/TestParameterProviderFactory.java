package de.quinscape.domainql;

import de.quinscape.domainql.beans.TestParamType;
import de.quinscape.domainql.param.ParameterProvider;
import de.quinscape.domainql.param.ParameterProviderFactory;

import java.lang.annotation.Annotation;

public class TestParameterProviderFactory
    implements ParameterProviderFactory
{
    @Override
    public ParameterProvider createIfApplicable(
        Class<?> parameterClass, Annotation[] annotations
    ) throws Exception
    {
        if (parameterClass.equals(TestParamType.class))
        {
            return new TestParameterProvider();
        }
        return null;
    }
}
