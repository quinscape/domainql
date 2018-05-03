package de.quinscape.domainql.fetcher;

import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final static Logger log = LoggerFactory.getLogger(SvensonFetcher.class);


    private final String name;


    public SvensonFetcher(String name)
    {
        this.name = name;   
    }


    @Override
    public Object get(DataFetchingEnvironment environment)
    {
        try
        {
            return JSONUtil.DEFAULT_UTIL.getProperty(environment.getSource(), name);
        }
        catch(Exception e)
        {
            log.error("Error in SvensonFetcher", e);
            return null;
        }
    }
}
