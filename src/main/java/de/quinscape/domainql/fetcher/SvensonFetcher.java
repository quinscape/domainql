package de.quinscape.domainql.fetcher;

import de.quinscape.domainql.util.JSONUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.svenson.JSONProperty;

/**
 * Accesses the underlying Java bean's property by the JSON name of the POJO classes in question. This will be the Java
 * property name by default but it can be changed with JSON property.
 *
 * @see JSONProperty
 */
public class SvensonFetcher
    implements DataFetcher<Object>
{
    private final String name;


    public SvensonFetcher(String name)
    {
        this.name = name;   
    }


    @Override
    public Object get(DataFetchingEnvironment environment)
    {
        return JSONUtil.DEFAULT_UTIL.getProperty(environment.getSource(), name);
    }
}
