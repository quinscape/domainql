/*
 * This file is generated by jOOQ.
 */
package de.quinscape.domainql.testdomain.tables;


import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.records.SourceOneRecord;

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
public class SourceOne extends TableImpl<SourceOneRecord> {

    private static final long serialVersionUID = 1L;

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
    public final TableField<SourceOneRecord, String> ID = createField(DSL.name("id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>public.source_one.target_id</code>.
     */
    public final TableField<SourceOneRecord, String> TARGET_ID = createField(DSL.name("target_id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    private SourceOne(Name alias, Table<SourceOneRecord> aliased) {
        this(alias, aliased, null);
    }

    private SourceOne(Name alias, Table<SourceOneRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
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

    /**
     * Create a <code>public.source_one</code> table reference
     */
    public SourceOne() {
        this(DSL.name("source_one"), null);
    }

    public <O extends Record> SourceOne(Table<O> child, ForeignKey<O, SourceOneRecord> key) {
        super(child, key, SOURCE_ONE);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<SourceOneRecord> getPrimaryKey() {
        return Keys.PK_SOURCE_ONE;
    }

    @Override
    public List<ForeignKey<SourceOneRecord, ?>> getReferences() {
        return Arrays.asList(Keys.SOURCE_ONE__FK_SOURCE_ONE_TARGET_ID);
    }

    private transient TargetOne _targetOne;

    /**
     * Get the implicit join path to the <code>public.target_one</code> table.
     */
    public TargetOne targetOne() {
        if (_targetOne == null)
            _targetOne = new TargetOne(this, Keys.SOURCE_ONE__FK_SOURCE_ONE_TARGET_ID);

        return _targetOne;
    }

    @Override
    public SourceOne as(String alias) {
        return new SourceOne(DSL.name(alias), this);
    }

    @Override
    public SourceOne as(Name alias) {
        return new SourceOne(alias, this);
    }

    @Override
    public SourceOne as(Table<?> alias) {
        return new SourceOne(alias.getQualifiedName(), this);
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

    /**
     * Rename this table
     */
    @Override
    public SourceOne rename(Table<?> name) {
        return new SourceOne(name.getQualifiedName(), null);
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
