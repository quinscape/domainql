package de.quinscape.domainql.preload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates all known preloaded queries for all entry points.
 * 
 */
public class PreloadedQueries
{
    private final static Logger log = LoggerFactory.getLogger(PreloadedQueries.class);

    private final Map<String, List<PreloadedQuery>> queries;


    public PreloadedQueries(
        Map<String, List<PreloadedQuery>> queries
    )
    {
        this.queries = queries;
    }


    /**
     * Returns the list of queries for the entry point.
     *
     * @param entryPoint
     * @return
     */
    public List<PreloadedQuery> getQueriesForEntryPoint(String entryPoint)
    {
        final List<PreloadedQuery> preloadedQueries = queries.get(entryPoint);

        if (preloadedQueries == null)
        {
            return Collections.emptyList();
        }

        return preloadedQueries;
    }


    public Map<String, List<PreloadedQuery>> getAllQueries()
    {
        return queries;
    }

}
