package de.quinscape.domainql.model;

import de.quinscape.domainql.generic.DomainObject;
import org.svenson.JSONParameter;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DomainType
    implements Model
{
    private final String name;

    private final String description;

    private final UniqueConstraint primaryKey;

    private final List<DomainField> fields;

    private final List<ForeignKey> foreignKeys;

    private final List<UniqueConstraint> uniqueConstraints;

    private final DTLayout layout;

    public DomainType(
        @JSONParameter("name")
        String name,

        @JSONParameter("description")
        String description,

        @JSONParameter("primaryKey")
        UniqueConstraint primaryKey,

        @JSONParameter("fields")
        @JSONTypeHint(DomainField.class)
        List<DomainField> fields,

        @JSONParameter("foreignKeys")
        List<ForeignKey> foreignKeys,

        @JSONParameter("uniqueConstraints")
        List<UniqueConstraint> uniqueConstraints,
        @JSONParameter("_")
        DTLayout layout
    )
    {
        this.description = description;
        this.uniqueConstraints = uniqueConstraints;
        this.layout = layout;
        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }

        if (fields == null)
        {
            throw new IllegalArgumentException("fields can't be null");
        }

        if (primaryKey == null)
        {
            throw new IllegalArgumentException("primaryKey can't be null");
        }

        if (foreignKeys == null)
        {
            throw new IllegalArgumentException("foreignKeys can't be null");
        }

        this.name = name;
        this.fields = fields;
        this.primaryKey = primaryKey;
        this.foreignKeys = foreignKeys;
    }


    @JSONProperty(priority = 100)
    @NotNull
    public String getName()
    {
        return name;
    }

    @JSONProperty(priority = 90, ignoreIfNull = true)
    public String getDescription()
    {
        return description;
    }

    @JSONProperty(priority = 80, ignoreIfNull = true)
    @NotNull
    public List<DomainField> getFields()
    {
        return fields;
    }


    @JSONProperty(priority = 70, ignoreIfNull = true)
    @NotNull
    public UniqueConstraint getPrimaryKey()
    {
        return primaryKey;
    }


    @JSONProperty(priority = 60, ignoreIfNull = true)
    @NotNull
    public List<ForeignKey> getForeignKeys()
    {
        return foreignKeys;
    }


    @JSONProperty(priority = 50, ignoreIfNull = true)
    @NotNull
    public List<UniqueConstraint> getUniqueConstraints()
    {
        return uniqueConstraints;
    }


    public static Builder newType()
    {
        return new Builder();
    }


    @JSONProperty("_")
    public DTLayout getLayout()
    {
        return layout;
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

        DomainType that = (DomainType) o;

        if (!name.equals(that.name))
        {
            return false;
        }
        if (!primaryKey.equals(that.primaryKey))
        {
            return false;
        }
        if (!fields.equals(that.fields))
        {
            return false;
        }
        return foreignKeys.equals(that.foreignKeys);
    }


    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + primaryKey.hashCode();
        result = 31 * result + fields.hashCode();
        result = 31 * result + foreignKeys.hashCode();
        return result;
    }


    public DomainField getField(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }


        for (DomainField field : fields)
        {
            if (field.getName().equals(name))
            {
                return field;
            }
        }

        return null;
    }


    public static class Builder
    {
        private static final List<DomainField> DEFAULT_FIELDS =
            Collections.singletonList(
            DomainField.newField()
                .withName(DomainObject.ID)
                .withType(FieldType.UUID)
                .withMaxLength(40)
                .build()
        );

        private String name;

        private List<DomainField> fields = new ArrayList<>();


        private UniqueConstraint primaryKey = UniqueConstraint.newPrimaryKey(DomainObject.ID);

        private List<ForeignKey> foreignKeys = new ArrayList<>();

        private String description;

        private List<UniqueConstraint> uniqueConstraints = new ArrayList<>();

        private DTLayout layout;


        public DomainType build()
        {
            return new DomainType(
                name,
                description,
                primaryKey,
                fields.size() == 0 ? DEFAULT_FIELDS : Collections.unmodifiableList(fields),
                Collections.unmodifiableList(foreignKeys),
                uniqueConstraints,
                layout
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


        public List<DomainField> getFields()
        {
            return fields;
        }


        public Builder withFields(DomainField... fields)
        {
            Collections.addAll(this.fields, fields);
            return this;
        }

        public Builder withFields(List<DomainField> fields)
        {
            this.fields.addAll(fields);
            return this;
        }


        public UniqueConstraint getPrimaryKey()
        {
            return primaryKey;
        }


        public Builder withPrimaryKey(String... fields)
        {
            final List<String> list = Arrays.asList(fields);
            return withPrimaryKey(list);
        }


        public Builder withPrimaryKey(List<String> list)
        {
            this.primaryKey = new UniqueConstraint(list);
            return this;
        }


        public List<ForeignKey> getForeignKeys()
        {
            return foreignKeys;
        }


        public Builder withForeignKeys(ForeignKey... foreignKeys)
        {
            Collections.addAll(this.foreignKeys, foreignKeys);
            return this;
        }

        public Builder withForeignKeys(List<ForeignKey> foreignKeys)
        {
            this.foreignKeys.addAll(foreignKeys);
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


        public Builder withUniqueConstraint(String... list)
        {
            return withUniqueConstraint(Arrays.asList(list));
        }

        public Builder withUniqueConstraint(List<String> list)
        {
            this.uniqueConstraints.add(new UniqueConstraint(list));
            return this;
        }


        public Builder withLayout(float x, float y)
        {
            this.layout = new DTLayout(x,y, null);
            return this;
        }

        public Builder withLayout(float x, float y, String color)
        {
            this.layout = new DTLayout(x,y, color);
            return this;
        }
    }




    public DomainField findField(String field)
    {
        for (DomainField domainField : fields)
        {
            if (domainField.getName().equals(field))
            {
                return domainField;
            }
        }
        return null;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            + ", description = '" + description + '\''
            + ", primaryKey = " + primaryKey
            + ", fields = " + fields
            + ", foreignKeys = " + foreignKeys
            ;
    }
}
