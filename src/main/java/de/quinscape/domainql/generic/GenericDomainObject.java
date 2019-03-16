package de.quinscape.domainql.generic;

import de.quinscape.domainql.fetcher.FetcherContext;
import org.svenson.DynamicProperties;
import org.svenson.JSONProperty;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A generic java side implementation of the domain object interface.
 */
public final class GenericDomainObject
    implements DynamicProperties, DomainObject
{

    private Map<String, Object> attrs;

    private FetcherContext fetcherContext;

    public GenericDomainObject()
    {
        this(null);
    }

    public GenericDomainObject(Map<String, Object> attrs)
    {
        this.attrs = attrs;
    }

    public Map<String, Object> contents()
    {
        return this.attrs;
    }

    @Override
    @NotNull
    @JSONProperty(value = DOMAIN_TYPE_PROPERTY, readOnly = true, priority = 1000)
    public String getDomainType()
    {
        return (String) getProperty(DOMAIN_TYPE_PROPERTY);
    }

    @Override
    public void setProperty(String name, Object value)
    {
        if (attrs == null)
        {
            attrs = new HashMap<>();
        }

        attrs.put(name, value);
    }


    @Override
    public Object getProperty(String name)
    {
        if (attrs == null)
        {
            return null;
        }

        return attrs.get(name);
    }


    @Override
    public Set<String> propertyNames()
    {
        if (attrs == null)
        {
            return Collections.emptySet();
        }

        return attrs.keySet();
    }


    @Override
    public boolean hasProperty(String name)
    {
        return attrs != null && attrs.containsKey(name);
    }


    @Override
    public Object removeProperty(String name)
    {
        throw new UnsupportedOperationException();
    }


    @JSONProperty(ignore = true)
    @Override
    public FetcherContext getFetcherContext()
    {
        return fetcherContext;
    }


    @Override
    public void setFetcherContext(FetcherContext fetcherContext)
    {
        this.fetcherContext = fetcherContext;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "attrs = " + attrs
            ;
    }
}
