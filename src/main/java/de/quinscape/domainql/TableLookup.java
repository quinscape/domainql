package de.quinscape.domainql;

import org.jooq.Table;

/**
 * Encapsulates the pojo class and the JOOQ table for one domain type
 */
public final class TableLookup
{
    private final Class<?> pojoType;
    private final Table<?> table;


    public TableLookup(Class<?> pojoType, Table<?> table)
    {
        if (pojoType == null)
        {
            throw new IllegalArgumentException("pojoType can't be null");
        }

        if (table == null)
        {
            throw new IllegalArgumentException("table can't be null");
        }


        this.pojoType = pojoType;
        this.table = table;
    }


    public Class<?> getPojoType()
    {
        return pojoType;
    }


    public Table<?> getTable()
    {
        return table;
    }

    public String getDomainType()
    {
        return pojoType.getSimpleName();
    }
}
