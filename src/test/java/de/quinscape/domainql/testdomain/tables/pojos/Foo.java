/*
 * This file is generated by jOOQ.
*/
package de.quinscape.domainql.testdomain.tables.pojos;


import de.quinscape.domainql.jooq.GeneratedDomainObject;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


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
@Entity
@Table(name = "foo", schema = "public", indexes = {
    @Index(name = "pk_foo", unique = true, columnList = "id ASC")
})
public class Foo extends GeneratedDomainObject implements Serializable {

    private static final long serialVersionUID = -1760514484;

    private String    id;
    private String    name;
    private Integer   num;
    private Timestamp created;

    public Foo() {}

    public Foo(Foo value) {
        this.id = value.id;
        this.name = value.name;
        this.num = value.num;
        this.created = value.created;
    }

    public Foo(
        String    id,
        String    name,
        Integer   num,
        Timestamp created
    ) {
        this.id = id;
        this.name = name;
        this.num = num;
        this.created = created;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false, length = 36)
    @NotNull
    @Size(max = 36)
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "name", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "num", nullable = false, precision = 32)
    @NotNull
    public Integer getNum() {
        return this.num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    @Column(name = "created", nullable = false)
    @NotNull
    public Timestamp getCreated() {
        return this.created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Foo (");

        sb.append(id);
        sb.append(", ").append(name);
        sb.append(", ").append(num);
        sb.append(", ").append(created);

        sb.append(")");
        return sb.toString();
    }
}
