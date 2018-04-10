package de.quinscape.domainql;

import org.jooq.ForeignKey;
import org.jooq.Table;

public class BackReference
{
    private final Table<?> table;
    private final ForeignKey<?,?> foreignKey;


    public BackReference(Table<?> table, ForeignKey<?, ?> foreignKey)
    {
        this.table = table;
        this.foreignKey = foreignKey;
    }


    public Table<?> getTable()
    {
        return table;
    }


    public ForeignKey<?, ?> getForeignKey()
    {
        return foreignKey;
    }
}
