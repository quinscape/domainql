package de.quinscape.domainql.jooq;

import de.quinscape.domainql.fetcher.FetcherContext;
import de.quinscape.domainql.generic.DomainObject;
import org.svenson.JSONProperty;

/**
 * Abstract base class for domain objects generated with the the {@link DomainObjectGeneratorStrategy}. Adds a
 * JSON-ignored fetcher context to the domain object to optionally optimize relation fetching
 */
public abstract class GeneratedDomainObject
    implements DomainObject
{
    private FetcherContext fetcherContext;

    @JSONProperty(value = DomainObject.FETCH_CONTEXT_PROPERTY, ignore = true)
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
}
