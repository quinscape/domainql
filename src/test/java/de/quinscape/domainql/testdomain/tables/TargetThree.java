/*
 * This file is generated by jOOQ.
*/
package de.quinscape.domainql.testdomain.tables;


import de.quinscape.domainql.testdomain.Indexes;
import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.records.TargetThreeRecord;

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
public class TargetThree extends TableImpl<TargetThreeRecord> {

    private static final long serialVersionUID = -1093030129;

    /**
     * The reference instance of <code>public.target_three</code>
     */
    public static final TargetThree TARGET_THREE = new TargetThree();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TargetThreeRecord> getRecordType() {
        return TargetThreeRecord.class;
    }

    /**
     * The column <code>public.target_three.id</code>.
     */
    public final TableField<TargetThreeRecord, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * Create a <code>public.target_three</code> table reference
     */
    public TargetThree() {
        this(DSL.name("target_three"), null);
    }

    /**
     * Create an aliased <code>public.target_three</code> table reference
     */
    public TargetThree(String alias) {
        this(DSL.name(alias), TARGET_THREE);
    }

    /**
     * Create an aliased <code>public.target_three</code> table reference
     */
    public TargetThree(Name alias) {
        this(alias, TARGET_THREE);
    }

    private TargetThree(Name alias, Table<TargetThreeRecord> aliased) {
        this(alias, aliased, null);
    }

    private TargetThree(Name alias, Table<TargetThreeRecord> aliased, Field<?>[] parameters) {
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
        return Arrays.<Index>asList(Indexes.PK_TARGET_THREE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<TargetThreeRecord> getPrimaryKey() {
        return Keys.PK_TARGET_THREE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<TargetThreeRecord>> getKeys() {
        return Arrays.<UniqueKey<TargetThreeRecord>>asList(Keys.PK_TARGET_THREE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TargetThree as(String alias) {
        return new TargetThree(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TargetThree as(Name alias) {
        return new TargetThree(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public TargetThree rename(String name) {
        return new TargetThree(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public TargetThree rename(Name name) {
        return new TargetThree(name, null);
    }
}
