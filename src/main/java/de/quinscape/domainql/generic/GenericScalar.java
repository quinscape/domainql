package de.quinscape.domainql.generic;

import de.quinscape.domainql.annotation.GraphQLScalar;

import java.util.Objects;

@GraphQLScalar
public class GenericScalar
{
    private String type;

    private Object value;


    public GenericScalar()
    {
        this(null, null);
    }


    public GenericScalar(String type, Object value)
    {
        this.type = type;
        this.value = value;
    }


    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    public Object getValue()
    {
        return value;
    }


    public void setValue(Object value)
    {
        this.value = value;
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
        GenericScalar that = (GenericScalar) o;
        return Objects.equals(type, that.type) &&
            Objects.equals(value, that.value);
    }


    /**
     * Returns true if the scalar value is of a list type.
     *
     * Does a quick check based on the GraphQL List-Type notation (e.g. "[Int]")
     *
     * @return true if the value is a list
     */
    public boolean isList()
    {
        return type.startsWith("[");
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(type, value);
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "type = '" + type + '\''
            + ", value = " + value
            ;
    }
}
