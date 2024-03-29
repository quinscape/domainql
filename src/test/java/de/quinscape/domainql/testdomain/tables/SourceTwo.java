/*
 * This file is generated by jOOQ.
 */
package de.quinscape.domainql.testdomain.tables;


import de.quinscape.domainql.testdomain.Keys;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.records.SourceTwoRecord;

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
public class SourceTwo extends TableImpl<SourceTwoRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.source_two</code>
     */
    public static final SourceTwo SOURCE_TWO = new SourceTwo();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SourceTwoRecord> getRecordType() {
        return SourceTwoRecord.class;
    }

    /**
     * The column <code>public.source_two.id</code>.
     */
    public final TableField<SourceTwoRecord, String> ID = createField(DSL.name("id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>public.source_two.target_id</code>.
     */
    public final TableField<SourceTwoRecord, String> TARGET_ID = createField(DSL.name("target_id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    private SourceTwo(Name alias, Table<SourceTwoRecord> aliased) {
        this(alias, aliased, null);
    }

    private SourceTwo(Name alias, Table<SourceTwoRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.source_two</code> table reference
     */
    public SourceTwo(String alias) {
        this(DSL.name(alias), SOURCE_TWO);
    }

    /**
     * Create an aliased <code>public.source_two</code> table reference
     */
    public SourceTwo(Name alias) {
        this(alias, SOURCE_TWO);
    }

    /**
     * Create a <code>public.source_two</code> table reference
     */
    public SourceTwo() {
        this(DSL.name("source_two"), null);
    }

    public <O extends Record> SourceTwo(Table<O> child, ForeignKey<O, SourceTwoRecord> key) {
        super(child, key, SOURCE_TWO);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<SourceTwoRecord> getPrimaryKey() {
        return Keys.PK_SOURCE_TWO;
    }

    @Override
    public List<ForeignKey<SourceTwoRecord, ?>> getReferences() {
        return Arrays.asList(Keys.SOURCE_TWO__FK_SOURCE_TWO_TARGET_ID);
    }

    private transient TargetTwo _targetTwo;

    /**
     * Get the implicit join path to the <code>public.target_two</code> table.
     */
    public TargetTwo targetTwo() {
        if (_targetTwo == null)
            _targetTwo = new TargetTwo(this, Keys.SOURCE_TWO__FK_SOURCE_TWO_TARGET_ID);

        return _targetTwo;
    }

    @Override
    public SourceTwo as(String alias) {
        return new SourceTwo(DSL.name(alias), this);
    }

    @Override
    public SourceTwo as(Name alias) {
        return new SourceTwo(alias, this);
    }

    @Override
    public SourceTwo as(Table<?> alias) {
        return new SourceTwo(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public SourceTwo rename(String name) {
        return new SourceTwo(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public SourceTwo rename(Name name) {
        return new SourceTwo(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public SourceTwo rename(Table<?> name) {
        return new SourceTwo(name.getQualifiedName(), null);
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
