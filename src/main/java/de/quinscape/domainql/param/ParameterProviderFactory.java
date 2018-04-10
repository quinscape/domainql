package de.quinscape.domainql.param;

import java.lang.annotation.Annotation;

/**
 * Implemented by classes creating parameter providers.
 */
public interface ParameterProviderFactory
{
    /**
     * Returns a parameter provider if this factory can create such a provider for a parameter with the given type and
     * annotations
     *
     * @param parameterClass    type
     * @param annotations       annotations
     *
     * @return Parameter provider or <code>null</code>
     */
    ParameterProvider createIfApplicable(Class<?> parameterClass, Annotation[] annotations) throws Exception;
}
