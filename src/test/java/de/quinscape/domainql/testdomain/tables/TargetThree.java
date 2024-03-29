/*
 * This file is generated by jOOQ.
 */
package de.quinscape.domainql.testdomain.tables;


import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.records.TargetThreeRecord;

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
public class TargetThree extends TableImpl<TargetThreeRecord> {

    private static final long serialVersionUID = 1L;

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
    public final TableField<TargetThreeRecord, String> ID = createField(DSL.name("id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    private TargetThree(Name alias, Table<TargetThreeRecord> aliased) {
        this(alias, aliased, null);
    }

    private TargetThree(Name alias, Table<TargetThreeRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
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

    /**
     * Create a <code>public.target_three</code> table reference
     */
    public TargetThree() {
        this(DSL.name("target_three"), null);
    }

    public <O extends Record> TargetThree(Table<O> child, ForeignKey<O, TargetThreeRecord> key) {
        super(child, key, TARGET_THREE);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<TargetThreeRecord> getPrimaryKey() {
        return Keys.PK_TARGET_THREE;
    }

    @Override
    public TargetThree as(String alias) {
        return new TargetThree(DSL.name(alias), this);
    }

    @Override
    public TargetThree as(Name alias) {
        return new TargetThree(alias, this);
    }

    @Override
    public TargetThree as(Table<?> alias) {
        return new TargetThree(alias.getQualifiedName(), this);
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

    /**
     * Rename this table
     */
    @Override
    public TargetThree rename(Table<?> name) {
        return new TargetThree(name.getQualifiedName(), null);
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
