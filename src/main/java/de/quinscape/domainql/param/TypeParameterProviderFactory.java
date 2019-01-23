package de.quinscape.domainql.param;

import de.quinscape.domainql.annotation.GraphQLTypeParam;

import java.lang.annotation.Annotation;

/**
 * Factory for the DataFetchingEnvironmentProvider.
 */
public class TypeParameterProviderFactory
    implements ParameterProviderFactory
{
    @Override
    public ParameterProvider createIfApplicable(
        Class<?> parameterClass, Annotation[] annotations
    )
    {
        for (int i = 0; i < annotations.length; i++)
        {
            Annotation annotation = annotations[i];
            if (annotation instanceof GraphQLTypeParam)
            {
                return TypeParameterProvider.INSTANCE;
            }
        }
        return null;
    }
}
