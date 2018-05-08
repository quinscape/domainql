package de.quinscape.domainql.model;

import org.svenson.JSONParameter;
import org.svenson.JSONProperty;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class EnumType
    implements Model
{
    private final String name;
    private final String description;

    private final List<String> values;


    public EnumType(
        @JSONParameter("name")
        String name,
        @JSONParameter("description")
        String description,
        @JSONParameter("values")
        List<String> values
    )
    {
        this.description = description;

        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }

        if (values == null)
        {
            throw new IllegalArgumentException("values can't be null");
        }

        this.name = name;
        this.values = values;
    }


    @NotNull
    @JSONProperty(priority = 100)
    public String getName()
    {
        return name;
    }

    @NotNull
    @JSONProperty(priority = 90)
    public List<String> getValues()
    {
        return values;
    }

    public static Builder newEnumType()
    {
        return new Builder();
    }

    public String getDescription()
    {
        return description;
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

        EnumType enumType = (EnumType) o;

        if (!name.equals(enumType.name))
        {
            return false;
        }
        return values.equals(enumType.values);
    }


    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }


    public static class Builder
    {
        private String name;

        private List<String> values;

        private String description;


        public EnumType build()
        {
            return new EnumType(
                name,
                description,
                Collections.unmodifiableList(values)
            );
        }


        public String getName()
        {
            return name;
        }


        public Builder withName(String name)
        {
            this.name = name;
            return this;
        }


        public List<String> getValues()
        {
            return values;
        }


        public Builder withValues(String... values)
        {
            Collections.addAll(this.values, values);
            return this;
        }

        public Builder withValues(List<String> values)
        {
            this.values.addAll(values);
            return this;
        }

        public String getDescription()
        {
            return description;
        }


        public Builder withDescription(String description)
        {
            this.description = description;
            return this;
        }
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            + ", description = '" + description + '\''
            + ", values = " + values
            ;
    }
}
