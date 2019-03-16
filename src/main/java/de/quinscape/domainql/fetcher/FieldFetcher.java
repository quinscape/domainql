package de.quinscape.domainql.fetcher;

import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONProperty;

/**
 * Accesses the underlying Java bean's property by the JSON fieldName of the POJO classes in question. This will be the Java
 * property fieldName by default but it can be changed with svenson's JSON property annotation.
 *
 * @see JSONProperty
 */
public class FieldFetcher
    implements DataFetcher<Object>
{
    private final static Logger log = LoggerFactory.getLogger(FieldFetcher.class);


    private final String domainType;

    private final String fieldName;


    public FieldFetcher(String domainType, String fieldName)
    {
        this.domainType = domainType;
        this.fieldName = fieldName;
    }


    @Override
    public Object get(DataFetchingEnvironment environment)
    {
        try
        {
            return JSONUtil.DEFAULT_UTIL.getProperty(environment.getSource(), fieldName);
        }
        catch(Exception e)
        {
            log.error("Error in FieldFetcher", e);
            return null;
        }
    }


    /**
     * Returns the domain type on which this fetcher is registered.
     *
     * @return domain type
     */
    public String getDomainType()
    {
        return domainType;
    }


    /**
     * Returns the field name that is fetched by this data fetcher.
     *
     * @return field name / JSON name
     */
    public String getFieldName()
    {
        return fieldName;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "fieldName = '" + fieldName + '\''
            ;
    }
}
