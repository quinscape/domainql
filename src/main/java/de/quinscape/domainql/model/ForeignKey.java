package de.quinscape.domainql.model;

import org.svenson.JSONParameter;
import org.svenson.JSONProperty;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class ForeignKey
{
    private final String description;
    private final List<String> fields;
    private final String targetType;
    private final List<String> targetFields;

    private final FKLayout layout;


    public ForeignKey(
        @JSONParameter("description")
            String description,
        @JSONParameter("fields")
            List<String> fields,
        @JSONParameter("targetType")
            String targetType,
        @JSONParameter("targetFields")
            List<String> targetFields,
        @JSONParameter("_")
        FKLayout layout
    )
    {
        this.description = description;
        this.targetFields = targetFields;
        this.layout = layout;

        if (fields == null)
        {
            throw new IllegalArgumentException("fields can't be null");
        }

        if (targetType == null)
        {
            throw new IllegalArgumentException("targetType can't be null");
        }

        this.fields = fields;
        this.targetType = targetType;
    }


    @NotNull
    public List<String> getFields()
    {
        return fields;
    }


    @NotNull
    public String getTargetType()
    {
        return targetType;
    }


    public String getDescription()
    {
        return description;
    }


    public static Builder newForeignKey()
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

        ForeignKey that = (ForeignKey) o;

        if (!fields.equals(that.fields))
        {
            return false;
        }
        return targetType.equals(that.targetType);
    }


    public List<String> getTargetFields()
    {
        return targetFields;
    }

    @JSONProperty("_")
    public FKLayout getLayout()
    {
        return layout;
    }


    @Override
    public int hashCode()
    {
        int result = fields.hashCode();
        result = 31 * result + targetType.hashCode();
        return result;
    }


    public static class Builder
    {
        private List<String> fields = new ArrayList<>();

        private String targetType;

        private String description;

        private List<String> targetFields = new ArrayList<>();

        private FKLayout layout;


        public ForeignKey build()
        {
            return new ForeignKey(
                description, fields,
                targetType,
                targetFields,
                layout
            );
        }


        @JSONProperty(priority = 100)
        public List<String> getFields()
        {
            return fields;
        }


        public Builder withField(String field)
        {
            this.fields.add(field);
            return this;
        }

        public Builder withFields(List<String> fields)
        {
            this.fields.addAll(fields);
            return this;
        }


        @JSONProperty(priority = 80)
        public String getTargetType()
        {
            return targetType;
        }


        public Builder withTargetType(String targetType)
        {
            this.targetType = targetType;
            return this;
        }

        @JSONProperty(priority = 90, ignoreIfNull = true)
        public String getDescription()
        {
            return description;
        }


        public Builder withDescription(String description)
        {
            this.description = description;
            return this;
        }


        public List<String> getTargetFields()
        {
            return targetFields;
        }


        public Builder withTargetFields(List<String> targetFields)
        {
            this.targetFields = targetFields;
            return this;
        }


        public Builder setLayout(float x, float y)
        {
            this.layout = new FKLayout(x,y);
            return this;
        }
    }

    @Override
    public String toString()
    {
        return super.toString() + ": "
            + ", description = '" + description + '\''
            + ", fields = " + fields
            + ", targetType = '" + targetType + '\''
            ;
    }
}
