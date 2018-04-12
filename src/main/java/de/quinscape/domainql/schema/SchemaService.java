package de.quinscape.domainql.schema;


import javax.sql.DataSource;

/**
 * Implemented by classes that synchronize an external storage schema with the current
 * application model.
 */
public interface SchemaService
{
    /**
     * Creates or updates the domain
     *
     */
    void synchronizeSchema(RuntimeContext runtimeContext, DataSource dataSource);

    /**
     * Removes the given schema
     */
    void removeSchema(RuntimeContext runtimeContext, DataSource dataSource, String schema);
}
