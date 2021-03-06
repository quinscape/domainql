/*
 * This file is generated by jOOQ.
*/
package de.quinscape.domainql.testdomain.tables;


import de.quinscape.domainql.testdomain.Indexes;
import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.records.SourceSixRecord;

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
public class SourceSix extends TableImpl<SourceSixRecord> {

    private static final long serialVersionUID = -696153102;

    /**
     * The reference instance of <code>public.source_six</code>
     */
    public static final SourceSix SOURCE_SIX = new SourceSix();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SourceSixRecord> getRecordType() {
        return SourceSixRecord.class;
    }

    /**
     * The column <code>public.source_six.id</code>.
     */
    public final TableField<SourceSixRecord, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>public.source_six.target_id</code>.
     */
    public final TableField<SourceSixRecord, String> TARGET_ID = createField("target_id", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * Create a <code>public.source_six</code> table reference
     */
    public SourceSix() {
        this(DSL.name("source_six"), null);
    }

    /**
     * Create an aliased <code>public.source_six</code> table reference
     */
    public SourceSix(String alias) {
        this(DSL.name(alias), SOURCE_SIX);
    }

    /**
     * Create an aliased <code>public.source_six</code> table reference
     */
    public SourceSix(Name alias) {
        this(alias, SOURCE_SIX);
    }

    private SourceSix(Name alias, Table<SourceSixRecord> aliased) {
        this(alias, aliased, null);
    }

    private SourceSix(Name alias, Table<SourceSixRecord> aliased, Field<?>[] parameters) {
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
        return Arrays.<Index>asList(Indexes.PK_SOURCE_SIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<SourceSixRecord> getPrimaryKey() {
        return Keys.PK_SOURCE_SIX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<SourceSixRecord>> getKeys() {
        return Arrays.<UniqueKey<SourceSixRecord>>asList(Keys.PK_SOURCE_SIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<SourceSixRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<SourceSixRecord, ?>>asList(Keys.SOURCE_SIX__FK_SOURCE_SIX_TARGET_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceSix as(String alias) {
        return new SourceSix(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceSix as(Name alias) {
        return new SourceSix(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public SourceSix rename(String name) {
        return new SourceSix(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public SourceSix rename(Name name) {
        return new SourceSix(name, null);
    }
}
