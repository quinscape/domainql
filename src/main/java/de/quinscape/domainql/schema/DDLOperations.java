package de.quinscape.domainql.schema;


import de.quinscape.domainql.model.DomainType;
import de.quinscape.domainql.model.ForeignKey;

import java.util.List;
import java.util.Map;

/**
 * Implemented by classes that provide data definition operations as used by the {@link DefaultSchemaService}.
 */
public interface DDLOperations
{
    /**
     * Lists the existing schemata.
     *
     * @return list of schemata
     */
    List<String> listSchemata(RuntimeContext runtimeContext);

    /**
     * Deletes a schema.
     *
     * @param name      schema name
     */
    void dropSchema(RuntimeContext runtimeContext, String name);

    /**
     * Creates a schema.
     *
     * @param name      schema name
     */
    void createSchema(RuntimeContext runtimeContext, String name);

    /**
     * Lists all existing tables of a given schema
     *
     * @param schema    schema name
     *
     * @return list of tables
     */
    List<String> listTables(RuntimeContext runtimeContext, String schema);

    /**
     * Creates a new database table for the given domain type.
     *
     * @param runtimeContext    runtime context
     * @param type              domain type
     */
    void createTable(RuntimeContext runtimeContext, DomainType type);

    /**
     * Updates the table for the given domain type.
     *
     * @param runtimeContext    runtime context
     * @param type              domain type
     */
    void updateTable(RuntimeContext runtimeContext, DomainType type);

    /**
     * Drops all keys for the given domain type / table.
     *
     * @param runtimeContext    runtime context
     * @param type              domain type
     */
    void dropKeys(RuntimeContext runtimeContext, DomainType type);

    /**
     * Creates the primary key for the given domain type / table.
     *
     * @param runtimeContext    runtime context
     * @param type              domain type
     */
    void createPrimaryKey(RuntimeContext runtimeContext, DomainType type);

    /**
     * Creates a foreign keys for the given domain type / table and domain property / field.
     *   @param runtimeContext        runtime context
     *  @param type                  domain type
     * @param domainProperty        domain property
     */
    void createForeignKey(RuntimeContext runtimeContext, DomainType type, ForeignKey domainProperty);

    void renameTable(RuntimeContext runtimeContext, String schema, String from, String to);

    Map<String, DatabaseColumn> listColumns(RuntimeContext runtimeContext, String schemaName, String tableName);

    void renameField(RuntimeContext runtimeContext, String schema, String type, String from, String to);

    void destroy();
}
