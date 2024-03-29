/*
 * This file is generated by jOOQ.
 */
package de.quinscape.domainql.testdomain.tables;


import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.records.SourceFiveRecord;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.annotation.processing.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function2;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row2;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.17.7"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SourceFive extends TableImpl<SourceFiveRecord> {

    private static final long serialVersionUID = 1L;

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
    public final TableField<SourceFiveRecord, String> ID = createField(DSL.name("id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>public.source_five.target_id</code>.
     */
    public final TableField<SourceFiveRecord, String> TARGET_ID = createField(DSL.name("target_id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    private SourceFive(Name alias, Table<SourceFiveRecord> aliased) {
        this(alias, aliased, null);
    }

    private SourceFive(Name alias, Table<SourceFiveRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
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

    /**
     * Create a <code>public.source_five</code> table reference
     */
    public SourceFive() {
        this(DSL.name("source_five"), null);
    }

    public <O extends Record> SourceFive(Table<O> child, ForeignKey<O, SourceFiveRecord> key) {
        super(child, key, SOURCE_FIVE);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<SourceFiveRecord> getPrimaryKey() {
        return Keys.PK_SOURCE_FIVE;
    }

    @Override
    public List<ForeignKey<SourceFiveRecord, ?>> getReferences() {
        return Arrays.asList(Keys.SOURCE_FIVE__FK_SOURCE_FIVE_TARGET_ID);
    }

    private transient TargetFive _targetFive;

    /**
     * Get the implicit join path to the <code>public.target_five</code> table.
     */
    public TargetFive targetFive() {
        if (_targetFive == null)
            _targetFive = new TargetFive(this, Keys.SOURCE_FIVE__FK_SOURCE_FIVE_TARGET_ID);

        return _targetFive;
    }

    @Override
    public SourceFive as(String alias) {
        return new SourceFive(DSL.name(alias), this);
    }

    @Override
    public SourceFive as(Name alias) {
        return new SourceFive(alias, this);
    }

    @Override
    public SourceFive as(Table<?> alias) {
        return new SourceFive(alias.getQualifiedName(), this);
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

    /**
     * Rename this table
     */
    @Override
    public SourceFive rename(Table<?> name) {
        return new SourceFive(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<String, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function2<? super String, ? super String, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function2<? super String, ? super String, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
