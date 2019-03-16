package de.quinscape.domainql.generic;

import de.quinscape.domainql.fetcher.FetcherContext;
import org.svenson.JSONProperty;

/**
 * Abstract base class for domain objects generated with the the {@link DomainObjectGeneratorStrategy}. Adds a
 * JSON-ignored fetcher context to the domain object to optionally optimize relation fetching
 */
public abstract class GeneratedDomainObject
    implements DomainObject
{
    private FetcherContext fetcherContext;

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
}
