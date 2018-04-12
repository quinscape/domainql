package de.quinscape.domainql.model;

import org.svenson.JSONParameter;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.ArrayList;
import java.util.List;

public class DomainField
    implements Model
{
    private final String name;
    private final String description;
    private final FieldType type;
    private final boolean required;
    private final int maxLength;
    private final String sqlType;
    private final List<ConfigValue> config;

    private final boolean unique;


    public DomainField(
        @JSONParameter("name")
        String name,

        @JSONParameter("description")
        String description,

        @JSONParameter("type")
        FieldType type,

        @JSONParameter("required")
        boolean required,

        @JSONParameter("maxLength")
        int maxLength,

        @JSONParameter("sqlType")
        String sqlType,

        @JSONParameter("config")
        @JSONTypeHint(ConfigValue.class)
        List<ConfigValue> config,

        @JSONParameter("unique")
        boolean unique
    )
    {
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = required;
        this.maxLength = maxLength;
        this.sqlType = sqlType;
        this.config = config;
        this.unique = unique;
    }

    @JSONProperty(priority = 100)
    public String getName()
    {
        return name;
    }

    @JSONProperty(priority = 90, ignoreIfNull = true)
    public String getDescription()
    {
        return description;
    }


    @JSONProperty(priority = 80)
    public FieldType getType()
    {
        return type;
    }


    @JSONProperty(priority = 70)
    public boolean isRequired()
    {
        return required;
    }


    @JSONProperty(priority = 60)
    public int getMaxLength()
    {
        return maxLength;
    }


    @JSONProperty(priority = 50, ignoreIfNull = true)
    public String getSqlType()
    {
        return sqlType;
    }


    @JSONProperty(priority = 40)
    public List<ConfigValue> getConfig()
    {
        return config;
    }

    public static Builder newField()
    {
        return new Builder();
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

        DomainField that = (DomainField) o;

        if (required != that.required)
        {
            return false;
        }
        if (maxLength != that.maxLength)
        {
            return false;
        }
        if (!name.equals(that.name))
        {
            return false;
        }
        if (type != that.type)
        {
            return false;
        }
        if (sqlType != null ? !sqlType.equals(that.sqlType) : that.sqlType != null)
        {
            return false;
        }
        return config != null ? config.equals(that.config) : that.config == null;
    }

    
    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (required ? 1 : 0);
        result = 31 * result + maxLength;
        result = 31 * result + (sqlType != null ? sqlType.hashCode() : 0);
        result = 31 * result + (config != null ? config.hashCode() : 0);
        return result;
    }


    public boolean isUnique()
    {
        return unique;
    }


    public static class Builder
    {
        private String name;

        private FieldType type = FieldType.STRING;

        private boolean required;

        private int maxLength;

        private String sqlType;

        private List<ConfigValue> config = new ArrayList<>();

        private ForeignKey foreignKey;

        private boolean unique;

        private String description;


        public DomainField build()
        {
            if (name == null)
            {
                throw new IllegalArgumentException("name can't be null");
            }

            if (type == null)
            {
                throw new IllegalArgumentException("type can't be null");
            }

            return new DomainField(
                name,
                description,
                type,
                required,
                maxLength,
                sqlType,
                config,
                unique
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


        public FieldType getType()
        {
            return type;
        }


        public Builder withType(FieldType type)
        {
            this.type = type;
            return this;
        }


        public boolean isRequired()
        {
            return required;
        }


        public Builder withRequired(boolean required)
        {
            this.required = required;
            return this;
        }

        public Builder isRequired(boolean required)
        {
            this.required = required;
            return this;
        }


        public int getMaxLength()
        {
            return maxLength;
        }


        public Builder withMaxLength(int maxLength)
        {
            this.maxLength = maxLength;
            return this;
        }


        public String getSqlType()
        {
            return sqlType;
        }


        public Builder withSqlType(String sqlType)
        {
            this.sqlType = sqlType;
            return this;
        }


        public ForeignKey getForeignKey()
        {
            return foreignKey;
        }


        public Builder withForeignKey(ForeignKey foreignKey)
        {
            this.foreignKey = foreignKey;
            return this;
        }


        public List<ConfigValue> getConfig()
        {
            return config;
        }


        public Builder withConfig(List<ConfigValue> data)
        {
            this.config.addAll(data);
            return this;
        }

        public <T> Builder withConfig(String name, String data)
        {
            this.config.add(new ConfigValue(name, data));
            return this;
        }


        public boolean isUnique()
        {
            return unique;
        }


        public Builder setUnique(boolean unique)
        {
            this.unique = unique;
            return this;
        }

        public Builder withUnique(boolean unique)
        {
            this.unique = unique;

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
            + ", type = " + type
            + ", required = " + required
            + ", maxLength = " + maxLength
            + ", sqlType = '" + sqlType + '\''
            + ", config = " + config
            + ", unique = " + unique
            ;
    }
}
