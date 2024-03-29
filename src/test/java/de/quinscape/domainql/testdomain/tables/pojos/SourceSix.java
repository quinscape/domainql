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
    name = "source_six",
    schema = "public"
)
public class SourceSix extends GeneratedDomainObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String targetId;

    public SourceSix() {}

    public SourceSix(SourceSix value) {
        this.id = value.id;
        this.targetId = value.targetId;
    }

    public SourceSix(
        String id,
        String targetId
    ) {
        this.id = id;
        this.targetId = targetId;
    }

    /**
     * Getter for <code>public.source_six.id</code>.
     */
    @Id
    @Column(name = "id", nullable = false, length = 36)
    @NotNull
    @Size(max = 36)
    public String getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.source_six.id</code>.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for <code>public.source_six.target_id</code>.
     */
    @Column(name = "target_id", nullable = false, length = 36)
    @NotNull
    @Size(max = 36)
    public String getTargetId() {
        return this.targetId;
    }

    /**
     * Setter for <code>public.source_six.target_id</code>.
     */
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SourceSix other = (SourceSix) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.targetId == null) {
            if (other.targetId != null)
                return false;
        }
        else if (!this.targetId.equals(other.targetId))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.targetId == null) ? 0 : this.targetId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SourceSix (");

        sb.append(id);
        sb.append(", ").append(targetId);

        sb.append(")");
        return sb.toString();
    }
}
