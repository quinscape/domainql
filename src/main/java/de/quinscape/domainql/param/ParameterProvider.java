package de.quinscape.domainql.param;

import graphql.schema.DataFetchingEnvironment;

public interface ParameterProvider<T>
{
    T provide(DataFetchingEnvironment environment);
}
