/*
 * This file is generated by jOOQ.
 */
package de.quinscape.domainql.testdomain.tables.pojos;


import de.quinscape.domainql.jooq.GeneratedDomainObject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

import javax.annotation.processing.Generated;


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
    name = "target_nine",
    schema = "public"
)
public class TargetNine extends GeneratedDomainObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;

    public TargetNine() {}

    public TargetNine(TargetNine value) {
        this.id = value.id;
        this.name = value.name;
    }

    public TargetNine(
        String id,
        String name
    ) {
        this.id = id;
        this.name = name;
    }

    /**
     * Getter for <code>public.target_nine.id</code>.
     */
    @Id
    @Column(name = "id", nullable = false, length = 36)
    @NotNull
    @Size(max = 36)
    public String getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.target_nine.id</code>.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for <code>public.target_nine.name</code>.
     */
    @Column(name = "name", nullable = false, length = 36)
    @NotNull
    @Size(max = 36)
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>public.target_nine.name</code>.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final TargetNine other = (TargetNine) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TargetNine (");

        sb.append(id);
        sb.append(", ").append(name);

        sb.append(")");
        return sb.toString();
    }
}
