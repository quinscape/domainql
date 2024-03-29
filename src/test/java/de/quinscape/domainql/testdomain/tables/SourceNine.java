/*
 * This file is generated by jOOQ.
 */
package de.quinscape.domainql.testdomain.tables;


import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.records.SourceNineRecord;

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
public class SourceNine extends TableImpl<SourceNineRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.source_nine</code>
     */
    public static final SourceNine SOURCE_NINE = new SourceNine();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SourceNineRecord> getRecordType() {
        return SourceNineRecord.class;
    }

    /**
     * The column <code>public.source_nine.id</code>.
     */
    public final TableField<SourceNineRecord, String> ID = createField(DSL.name("id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>public.source_nine.target_id</code>.
     */
    public final TableField<SourceNineRecord, String> TARGET_ID = createField(DSL.name("target_id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    private SourceNine(Name alias, Table<SourceNineRecord> aliased) {
        this(alias, aliased, null);
    }

    private SourceNine(Name alias, Table<SourceNineRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.source_nine</code> table reference
     */
    public SourceNine(String alias) {
        this(DSL.name(alias), SOURCE_NINE);
    }

    /**
     * Create an aliased <code>public.source_nine</code> table reference
     */
    public SourceNine(Name alias) {
        this(alias, SOURCE_NINE);
    }

    /**
     * Create a <code>public.source_nine</code> table reference
     */
    public SourceNine() {
        this(DSL.name("source_nine"), null);
    }

    public <O extends Record> SourceNine(Table<O> child, ForeignKey<O, SourceNineRecord> key) {
        super(child, key, SOURCE_NINE);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<SourceNineRecord> getPrimaryKey() {
        return Keys.PK_SOURCE_NINE;
    }

    @Override
    public List<ForeignKey<SourceNineRecord, ?>> getReferences() {
        return Arrays.asList(Keys.SOURCE_NINE__FK_SOURCE_NINE_TARGET_ID);
    }

    private transient TargetNine _targetNine;

    /**
     * Get the implicit join path to the <code>public.target_nine</code> table.
     */
    public TargetNine targetNine() {
        if (_targetNine == null)
            _targetNine = new TargetNine(this, Keys.SOURCE_NINE__FK_SOURCE_NINE_TARGET_ID);

        return _targetNine;
    }

    @Override
    public SourceNine as(String alias) {
        return new SourceNine(DSL.name(alias), this);
    }

    @Override
    public SourceNine as(Name alias) {
        return new SourceNine(alias, this);
    }

    @Override
    public SourceNine as(Table<?> alias) {
        return new SourceNine(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public SourceNine rename(String name) {
        return new SourceNine(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public SourceNine rename(Name name) {
        return new SourceNine(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public SourceNine rename(Table<?> name) {
        return new SourceNine(name.getQualifiedName(), null);
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
