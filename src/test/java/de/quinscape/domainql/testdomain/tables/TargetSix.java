/*
 * This file is generated by jOOQ.
*/
package de.quinscape.domainql.testdomain.tables;


import de.quinscape.domainql.testdomain.Indexes;
import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.records.TargetSixRecord;
import org.jooq.Field;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import javax.annotation.Generated;
import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.6"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TargetSix extends TableImpl<TargetSixRecord> {

    private static final long serialVersionUID = 1148476199;

    /**
     * The reference instance of <code>public.target_six</code>
     */
    public static final TargetSix TARGET_SIX = new TargetSix();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TargetSixRecord> getRecordType() {
        return TargetSixRecord.class;
    }

    /**
     * The column <code>public.target_six.id</code>.
     */
    public final TableField<TargetSixRecord, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * Create a <code>public.target_six</code> table reference
     */
    public TargetSix() {
        this(DSL.name("target_six"), null);
    }

    /**
     * Create an aliased <code>public.target_six</code> table reference
     */
    public TargetSix(String alias) {
        this(DSL.name(alias), TARGET_SIX);
    }

    /**
     * Create an aliased <code>public.target_six</code> table reference
     */
    public TargetSix(Name alias) {
        this(alias, TARGET_SIX);
    }

    private TargetSix(Name alias, Table<TargetSixRecord> aliased) {
        this(alias, aliased, null);
    }

    private TargetSix(Name alias, Table<TargetSixRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.PK_TARGET_SIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<TargetSixRecord> getPrimaryKey() {
        return Keys.PK_TARGET_SIX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<TargetSixRecord>> getKeys() {
        return Arrays.<UniqueKey<TargetSixRecord>>asList(Keys.PK_TARGET_SIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TargetSix as(String alias) {
        return new TargetSix(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TargetSix as(Name alias) {
        return new TargetSix(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public TargetSix rename(String name) {
        return new TargetSix(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public TargetSix rename(Name name) {
        return new TargetSix(name, null);
    }
}
