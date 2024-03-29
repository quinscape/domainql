/*
 * This file is generated by jOOQ.
 */
package de.quinscape.domainql.testdomain.tables.records;


import de.quinscape.domainql.testdomain.tables.BarOrg;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import javax.annotation.processing.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


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
@Entity
@Table(
    name = "bar_org",
    schema = "public"
)
public class BarOrgRecord extends UpdatableRecordImpl<BarOrgRecord> implements Record2<String, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.bar_org.id</code>.
     */
    public void setId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.bar_org.id</code>.
     */
    @Id
    @Column(name = "id", nullable = false, length = 36)
    @NotNull
    @Size(max = 36)
    public String getId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.bar_org.name</code>.
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.bar_org.name</code>.
     */
    @Column(name = "name", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    public String getName() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<String, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<String, String> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return BarOrg.BAR_ORG.ID;
    }

    @Override
    public Field<String> field2() {
        return BarOrg.BAR_ORG.NAME;
    }

    @Override
    public String component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getName();
    }

    @Override
    public String value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getName();
    }

    @Override
    public BarOrgRecord value1(String value) {
        setId(value);
        return this;
    }

    @Override
    public BarOrgRecord value2(String value) {
        setName(value);
        return this;
    }

    @Override
    public BarOrgRecord values(String value1, String value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached BarOrgRecord
     */
    public BarOrgRecord() {
        super(BarOrg.BAR_ORG);
    }

    /**
     * Create a detached, initialised BarOrgRecord
     */
    public BarOrgRecord(String id, String name) {
        super(BarOrg.BAR_ORG);

        setId(id);
        setName(name);
    }

    /**
     * Create a detached, initialised BarOrgRecord
     */
    public BarOrgRecord(de.quinscape.domainql.testdomain.tables.pojos.BarOrg value) {
        super(BarOrg.BAR_ORG);

        if (value != null) {
            setId(value.getId());
            setName(value.getName());
        }
    }
}
