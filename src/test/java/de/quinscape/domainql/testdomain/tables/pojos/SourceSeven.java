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
    name = "source_seven",
    schema = "public"
)
public class SourceSeven extends GeneratedDomainObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String target;

    public SourceSeven() {}

    public SourceSeven(SourceSeven value) {
        this.id = value.id;
        this.target = value.target;
    }

    public SourceSeven(
        String id,
        String target
    ) {
        this.id = id;
        this.target = target;
    }

    /**
     * Getter for <code>public.source_seven.id</code>.
     */
    @Id
    @Column(name = "id", nullable = false, length = 36)
    @NotNull
    @Size(max = 36)
    public String getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.source_seven.id</code>.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for <code>public.source_seven.target</code>.
     */
    @Column(name = "target", nullable = false, length = 36)
    @NotNull
    @Size(max = 36)
    public String getTarget() {
        return this.target;
    }

    /**
     * Setter for <code>public.source_seven.target</code>.
     */
    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SourceSeven other = (SourceSeven) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.target == null) {
            if (other.target != null)
                return false;
        }
        else if (!this.target.equals(other.target))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.target == null) ? 0 : this.target.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SourceSeven (");

        sb.append(id);
        sb.append(", ").append(target);

        sb.append(")");
        return sb.toString();
    }
}
