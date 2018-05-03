package de.quinscape.domainql.preload;

import java.util.Map;

/**
 * Encapsulates a single preloaded query.
 *
 */
public class PreloadedQuery
{
    private final String name;

    private final Map<String, Object> query;


    /**
     * Creates a new PreloadedQuery instance
     *
     * @param name      name under which the query result will be stored
     * @param query     query map containing the query string as <code>"query"</code> and potentially static variables as <code>"variables"</code>
     */
    public PreloadedQuery(String name, Map<String, Object> query)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }

        if (query == null)
        {
            throw new IllegalArgumentException("query can't be null");
        }

        this.name = name;
        this.query = query;
    }


    public String getName()
    {
        return name;
    }


    public Map<String, Object> getQuery()
    {
        return query;
    }
}
