package de.quinscape.domainql.generic;

import de.quinscape.domainql.fetcher.FetcherContext;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.svenson.JSONProperty;

import jakarta.validation.constraints.NotNull;
import java.util.Set;

/**
 * Optional generic interface for schema domain types. Allows transmitting any domain type as "DomainObject" scalar.
 */
public interface DomainObject
{
    String DOMAIN_TYPE_PROPERTY = "_type";

    String ID = "id";

    @NotNull
    @JSONProperty(value = DOMAIN_TYPE_PROPERTY, readOnly = true, priority = 1000)
    default String getDomainType()
    {
        return this.getClass().getSimpleName();
    }

    default Object getProperty(String name)
    {
        return JSONUtil.DEFAULT_UTIL.getProperty(this, name);
    }

    default void setProperty(String name, Object value)
    {
        JSONUtil.DEFAULT_UTIL.setProperty(this, name, value);
    }
    
    default Set<String> propertyNames()
    {
        final Set<String> allPropertyNames = JSONUtil.DEFAULT_UTIL.getAllPropertyNames(this);
        allPropertyNames.remove(DOMAIN_TYPE_PROPERTY);
        return allPropertyNames;
    }

    FetcherContext lookupFetcherContext();

    void provideFetcherContext(FetcherContext fetcherContext);
}

