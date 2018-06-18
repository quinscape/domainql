/*
 * This file is generated by jOOQ.
*/
package de.quinscape.domainql.testdomain.tables;


import de.quinscape.domainql.testdomain.Indexes;
import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.records.SourceOneRecord;
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
public class SourceOne extends TableImpl<SourceOneRecord> {

    private static final long serialVersionUID = -1918556822;

    /**
     * The reference instance of <code>public.source_one</code>
     */
    public static final SourceOne SOURCE_ONE = new SourceOne();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SourceOneRecord> getRecordType() {
        return SourceOneRecord.class;
    }

    /**
     * The column <code>public.source_one.id</code>.
     */
    public final TableField<SourceOneRecord, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>public.source_one.target_id</code>.
     */
    public final TableField<SourceOneRecord, String> TARGET_ID = createField("target_id", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * Create a <code>public.source_one</code> table reference
     */
    public SourceOne() {
        this(DSL.name("source_one"), null);
    }

    /**
     * Create an aliased <code>public.source_one</code> table reference
     */
    public SourceOne(String alias) {
        this(DSL.name(alias), SOURCE_ONE);
    }

    /**
     * Create an aliased <code>public.source_one</code> table reference
     */
    public SourceOne(Name alias) {
        this(alias, SOURCE_ONE);
    }

    private SourceOne(Name alias, Table<SourceOneRecord> aliased) {
        this(alias, aliased, null);
    }

    private SourceOne(Name alias, Table<SourceOneRecord> aliased, Field<?>[] parameters) {
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
        return Arrays.<Index>asList(Indexes.PK_SOURCE_ONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<SourceOneRecord> getPrimaryKey() {
        return Keys.PK_SOURCE_ONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<SourceOneRecord>> getKeys() {
        return Arrays.<UniqueKey<SourceOneRecord>>asList(Keys.PK_SOURCE_ONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<SourceOneRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<SourceOneRecord, ?>>asList(Keys.SOURCE_ONE__FK_SOURCE_ONE_TARGET_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceOne as(String alias) {
        return new SourceOne(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceOne as(Name alias) {
        return new SourceOne(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public SourceOne rename(String name) {
        return new SourceOne(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public SourceOne rename(Name name) {
        return new SourceOne(name, null);
    }
}
