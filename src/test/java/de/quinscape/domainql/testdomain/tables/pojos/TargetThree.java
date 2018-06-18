/*
 * This file is generated by jOOQ.
*/
package de.quinscape.domainql.testdomain.tables.pojos;


import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;


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
@Table(name = "target_three", schema = "public", indexes = {
    @Index(name = "pk_target_three", unique = true, columnList = "id ASC")
})
public class TargetThree implements Serializable {

    private static final long serialVersionUID = -13197833;

    private String id;

    public TargetThree() {}

    public TargetThree(TargetThree value) {
        this.id = value.id;
    }

    public TargetThree(
        String id
    ) {
        this.id = id;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TargetThree (");

        sb.append(id);

        sb.append(")");
        return sb.toString();
    }
}
