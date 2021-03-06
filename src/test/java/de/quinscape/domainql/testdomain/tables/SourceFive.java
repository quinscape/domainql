/*
 * This file is generated by jOOQ.
*/
package de.quinscape.domainql.testdomain.tables;


import de.quinscape.domainql.testdomain.Indexes;
import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.records.SourceFiveRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
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
public class SourceFive extends TableImpl<SourceFiveRecord> {

    private static final long serialVersionUID = 1578467810;

    /**
     * The reference instance of <code>public.source_five</code>
     */
    public static final SourceFive SOURCE_FIVE = new SourceFive();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SourceFiveRecord> getRecordType() {
        return SourceFiveRecord.class;
    }

    /**
     * The column <code>public.source_five.id</code>.
     */
    public final TableField<SourceFiveRecord, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>public.source_five.target_id</code>.
     */
    public final TableField<SourceFiveRecord, String> TARGET_ID = createField("target_id", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * Create a <code>public.source_five</code> table reference
     */
    public SourceFive() {
        this(DSL.name("source_five"), null);
    }

    /**
     * Create an aliased <code>public.source_five</code> table reference
     */
    public SourceFive(String alias) {
        this(DSL.name(alias), SOURCE_FIVE);
    }

    /**
     * Create an aliased <code>public.source_five</code> table reference
     */
    public SourceFive(Name alias) {
        this(alias, SOURCE_FIVE);
    }

    private SourceFive(Name alias, Table<SourceFiveRecord> aliased) {
        this(alias, aliased, null);
    }

    private SourceFive(Name alias, Table<SourceFiveRecord> aliased, Field<?>[] parameters) {
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
        return Arrays.<Index>asList(Indexes.PK_SOURCE_FIVE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<SourceFiveRecord> getPrimaryKey() {
        return Keys.PK_SOURCE_FIVE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<SourceFiveRecord>> getKeys() {
        return Arrays.<UniqueKey<SourceFiveRecord>>asList(Keys.PK_SOURCE_FIVE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<SourceFiveRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<SourceFiveRecord, ?>>asList(Keys.SOURCE_FIVE__FK_SOURCE_FIVE_TARGET_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceFive as(String alias) {
        return new SourceFive(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceFive as(Name alias) {
        return new SourceFive(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public SourceFive rename(String name) {
        return new SourceFive(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public SourceFive rename(Name name) {
        return new SourceFive(name, null);
    }
}
