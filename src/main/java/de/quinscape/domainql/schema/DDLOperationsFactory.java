package de.quinscape.domainql.schema;


import javax.sql.DataSource;

/**
 * Creates DDLOperations instances.
 */
public interface DDLOperationsFactory
{
    DDLOperations create(RuntimeContext runtimeContext, DataSource dataSource);
}
