package de.quinscape.domainql;

import de.quinscape.domainql.config.RelationConfiguration;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import org.jooq.TableField;


public class RelationBuilder
{
    private String id;

    private TableField<?, ?> fkField;

    private SourceField sourceField;
    
    private TargetField targetField;


    public RelationBuilder(String id, TableField<?, ?> fkField)
    {
        this.fkField = fkField;
        if (id == null && fkField == null)
        {
            throw new IllegalStateException("Need either a relation id or an foreign key field");
        }
        this.id = id;
    }

    public RelationConfiguration build()
    {
        return null;
    }
}
