package de.quinscape.domainql.schema;

import javax.sql.DataSource;

public class NoopSchemaService
    implements SchemaService
{
    @Override
    public void synchronizeSchema(
        RuntimeContext runtimeContext, DataSource dataSource
    )
    {
        
    }


    @Override
    public void removeSchema(RuntimeContext runtimeContext, DataSource dataSource, String schema)
    {

    }
}
