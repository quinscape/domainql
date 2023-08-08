/*
 * This file is generated by jOOQ.
 */
package de.quinscape.domainql.testdomain.tables;


import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.records.TargetTwoRecord;

import java.util.function.Function;

import javax.annotation.processing.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function1;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row1;
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
public class TargetTwo extends TableImpl<TargetTwoRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.target_two</code>
     */
    public static final TargetTwo TARGET_TWO = new TargetTwo();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TargetTwoRecord> getRecordType() {
        return TargetTwoRecord.class;
    }

    /**
     * The column <code>public.target_two.id</code>.
     */
    public final TableField<TargetTwoRecord, String> ID = createField(DSL.name("id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    private TargetTwo(Name alias, Table<TargetTwoRecord> aliased) {
        this(alias, aliased, null);
    }

    private TargetTwo(Name alias, Table<TargetTwoRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.target_two</code> table reference
     */
    public TargetTwo(String alias) {
        this(DSL.name(alias), TARGET_TWO);
    }

    /**
     * Create an aliased <code>public.target_two</code> table reference
     */
    public TargetTwo(Name alias) {
        this(alias, TARGET_TWO);
    }

    /**
     * Create a <code>public.target_two</code> table reference
     */
    public TargetTwo() {
        this(DSL.name("target_two"), null);
    }

    public <O extends Record> TargetTwo(Table<O> child, ForeignKey<O, TargetTwoRecord> key) {
        super(child, key, TARGET_TWO);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<TargetTwoRecord> getPrimaryKey() {
        return Keys.PK_TARGET_TWO;
    }

    @Override
    public TargetTwo as(String alias) {
        return new TargetTwo(DSL.name(alias), this);
    }

    @Override
    public TargetTwo as(Name alias) {
        return new TargetTwo(alias, this);
    }

    @Override
    public TargetTwo as(Table<?> alias) {
        return new TargetTwo(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public TargetTwo rename(String name) {
        return new TargetTwo(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public TargetTwo rename(Name name) {
        return new TargetTwo(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public TargetTwo rename(Table<?> name) {
        return new TargetTwo(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row1 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row1<String> fieldsRow() {
        return (Row1) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function1<? super String, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function1<? super String, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
