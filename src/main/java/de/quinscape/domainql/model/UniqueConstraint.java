package de.quinscape.domainql.model;

import org.svenson.JSONParameter;

import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

public class UniqueConstraint
{
    private final List<String> fields;


    public UniqueConstraint(
        @JSONParameter("fields")
        List<String> fields
    )
    {
        if (fields == null)
        {
            throw new IllegalArgumentException("fields can't be null");
        }

        this.fields = fields;
    }

    @NotNull
    public List<String> getFields()
    {
        return fields;
    }

    public static UniqueConstraint newPrimaryKey(String... fields)
    {
        return newPrimaryKey(Arrays.asList(fields));
    }

    public static UniqueConstraint newPrimaryKey(List<String> fields)
    {
        return new UniqueConstraint(fields);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        UniqueConstraint that = (UniqueConstraint) o;

        return fields.equals(that.fields);
    }


    @Override
    public int hashCode()
    {
        return fields.hashCode();
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "fields = " + fields
            ;
    }
}
