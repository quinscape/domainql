package de.quinscape.domainql.schema;

import de.quinscape.domainql.model.Domain;

public class RuntimeContext
{
    private final Domain domain;

    private final String schemaName;


    public RuntimeContext(Domain domain, String schemaName)
    {
        this.domain = domain;
        this.schemaName = schemaName;
    }


    public Domain getDomain()
    {
        return domain;
    }


    public String getSchemaName()
    {
        return schemaName;
    }
}
