/*
 * This file is generated by jOOQ.
*/
package de.quinscape.domainql.testdomain.tables;


import de.quinscape.domainql.testdomain.Indexes;
import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.records.TargetNineRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


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
public class TargetNine extends TableImpl<TargetNineRecord> {

    private static final long serialVersionUID = -1400843815;

    /**
     * The reference instance of <code>public.target_nine</code>
     */
    public static final TargetNine TARGET_NINE = new TargetNine();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TargetNineRecord> getRecordType() {
        return TargetNineRecord.class;
    }

    /**
     * The column <code>public.target_nine.id</code>.
     */
    public final TableField<TargetNineRecord, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>public.target_nine.name</code>.
     */
    public final TableField<TargetNineRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * Create a <code>public.target_nine</code> table reference
     */
    public TargetNine() {
        this(DSL.name("target_nine"), null);
    }

    /**
     * Create an aliased <code>public.target_nine</code> table reference
     */
    public TargetNine(String alias) {
        this(DSL.name(alias), TARGET_NINE);
    }

    /**
     * Create an aliased <code>public.target_nine</code> table reference
     */
    public TargetNine(Name alias) {
        this(alias, TARGET_NINE);
    }

    private TargetNine(Name alias, Table<TargetNineRecord> aliased) {
        this(alias, aliased, null);
    }

    private TargetNine(Name alias, Table<TargetNineRecord> aliased, Field<?>[] parameters) {
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
        return Arrays.<Index>asList(Indexes.PK_TARGET_NINE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<TargetNineRecord> getPrimaryKey() {
        return Keys.PK_TARGET_NINE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<TargetNineRecord>> getKeys() {
        return Arrays.<UniqueKey<TargetNineRecord>>asList(Keys.PK_TARGET_NINE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TargetNine as(String alias) {
        return new TargetNine(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TargetNine as(Name alias) {
        return new TargetNine(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public TargetNine rename(String name) {
        return new TargetNine(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public TargetNine rename(Name name) {
        return new TargetNine(name, null);
    }
}
