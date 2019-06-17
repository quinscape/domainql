package de.quinscape.domainql.jooq;

import de.quinscape.domainql.fetcher.FetcherContext;
import de.quinscape.domainql.generic.DomainObject;

/**
 * Abstract base class for domain objects generated with the the {@link DomainObjectGeneratorStrategy}. Adds a
 * JSON-ignored fetcher context to the domain object to optionally optimize relation fetching
 */
public abstract class GeneratedDomainObject
    implements DomainObject
{
    private FetcherContext fetcherContext;

    @Override
    public FetcherContext lookupFetcherContext()
    {
        return fetcherContext;
    }


    @Override
    public void provideFetcherContext(FetcherContext fetcherContext)
    {
        this.fetcherContext = fetcherContext;
    }
}
