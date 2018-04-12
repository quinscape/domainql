package de.quinscape.domainql.model;

import de.quinscape.domainql.DomainQLException;
import org.svenson.JSONParameter;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Domain
{
    private final String description;
    private final List<DomainType> domainTypes;
    private final List<EnumType> enumTypes;
    private final String layoutData;

    @JSONProperty(priority = 1000, readOnly = true)
    public String getDomainType()
    {
        return "domainQL.Domain";
    }

    public Domain(

        @JSONParameter("description")
        String description,

        @JSONParameter("domainTypes")
        @JSONTypeHint(DomainType.class)
        List<DomainType> domainTypes,

        @JSONParameter("enumTypes")
        @JSONTypeHint(EnumType.class)
        List<EnumType> enumTypes,

        @JSONParameter("layoutData")
        String layoutData
    )
    {
        if (domainTypes == null)
        {
            throw new IllegalArgumentException("domainTypes can't be null");
        }

        if (enumTypes == null)
        {
            throw new IllegalArgumentException("enumTypes can't be null");
        }

        this.description = description;
        this.domainTypes = domainTypes;
        this.enumTypes = enumTypes;
        this.layoutData = layoutData;
    }


    @JSONProperty(priority = 90, ignoreIfNull = true)
    public String getDescription()
    {
        return description;
    }

    @JSONProperty(priority = 80)
    public List<DomainType> getDomainTypes()
    {
        return domainTypes;
    }


    @JSONProperty(priority = 70)
    public List<EnumType> getEnumTypes()
    {
        return enumTypes;
    }


    @JSONProperty(priority = 60)
    public String getLayoutData()
    {
        return layoutData;
    }


    public static Builder newDomain()
    {
        return new Builder();
    }

    public DomainType getDomainType(String name)
    {
        return getDomainType(name, domainTypes);
    }

    static DomainType getDomainType(String name, List<DomainType> domainTypes)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }

        for (DomainType type : domainTypes)
        {
            if (type.getName().equals(name))
            {
                return type;
            }
        }

        return null;
    }

    public EnumType getEnumType(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }


        for (EnumType type : enumTypes)
        {
            if (type.getName().equals(name))
            {
                return type;
            }
        }

        return null;
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

        Domain domain = (Domain) o;

        if (!domainTypes.equals(domain.domainTypes))
        {
            return false;
        }
        if (!enumTypes.equals(domain.enumTypes))
        {
            return false;
        }
        return layoutData != null ? layoutData.equals(domain.layoutData) : domain.layoutData == null;
    }



    @Override
    public int hashCode()
    {
        int result = domainTypes.hashCode();
        result = 31 * result + enumTypes.hashCode();
        result = 31 * result + (layoutData != null ? layoutData.hashCode() : 0);
        return result;
    }



    public static void ensureUniqueness(List<? extends Model> domainTypes)
    {
        Map<String,Model> names = new HashMap<>();

        for (Model domainType : domainTypes)
        {
            String name = domainType.getName();
            final Model existing = names.get(name);
            if (existing != null)
            {
                throw new IllegalStateException(domainType + " and " + existing + " have the same name");
            }
        }
    }

    public Domain merge(
        String description,
        Domain that)
    {
        final Domain merged = Domain.newDomain()
            .withDescription(description)
            .withTypes(this.domainTypes.toArray(new DomainType[0]))
            .withTypes(that.domainTypes.toArray(new DomainType[0]))
            .withEnumTypes(this.enumTypes.toArray(new EnumType[0]))
            .withEnumTypes(that.enumTypes.toArray(new EnumType[0]))
            .build();

        return merged;
    }

    @Override
    public String toString()
    {
        return super.toString() + ": "
            + ", description = '" + description + '\''
            + ", domainTypes = " + domainTypes
            + ", enumTypes = " + enumTypes
            ;
    }

    public static class Builder
    {
        private List<DomainType> domainTypes = new ArrayList<>();

        private String layoutData = "{}";

        private List<EnumType> enumTypes = new ArrayList<>();

        private String description;


        public Domain build()
        {
            validate();

            return new Domain(
                description,
                Collections.unmodifiableList(domainTypes),
                Collections.unmodifiableList(enumTypes),
                layoutData
            );
        }


        public List<DomainType> getDomainTypes()
        {
            return domainTypes;
        }


        public Builder withTypes(DomainType... domainType)
        {
            Collections.addAll(this.domainTypes, domainType);
            return this;
        }


        public Builder withTypes(List<DomainType> domainType)
        {
            this.domainTypes.addAll(domainType);
            return this;
        }

        public List<EnumType> getEnumTypes()
        {
            return enumTypes;
        }


        public Builder withEnumTypes(EnumType... enumTypes)
        {
            Collections.addAll(this.enumTypes, enumTypes);
            return this;
        }

        public Builder withEnumTypes(List<EnumType> enumTypes)
        {
            this.enumTypes.addAll(enumTypes);
            return this;
        }


        public String getLayoutData()
        {
            return layoutData;
        }


        public Builder withLayoutData(String layoutData)
        {
            this.layoutData = layoutData;
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

        public void validate()
        {
            ensureUniqueness(this.domainTypes);
            ensureUniqueness(this.enumTypes);

            this.domainTypes.forEach(this::validateDomainType);
            this.enumTypes.forEach(this::validateEnumType);
        }


        private void validateEnumType(EnumType enumType)
        {
            final List<String> values = enumType.getValues();
            final Set<String> set = new HashSet<>();
            for (String value : values)
            {
                if (set.contains(value))
                {
                    throw new IllegalStateException(value + " appears more than once in list of values: " + enumType);
                }
                set.add(value);
            }
        }


        private void validateDomainType(DomainType domainType)
        {
            Domain.ensureUniqueness(domainType.getFields());

            final List<String> primaryKeyFields = domainType.getPrimaryKey().getFields();
            primaryKeyFields.forEach(fieldName -> {
                final DomainField field = domainType.findField(fieldName);
                if (field == null)
                {
                    throw new DomainQLException("Primary key references non-existing field '" + fieldName + "': " + this);
                }
            });

            domainType.getForeignKeys().forEach(foreignKey -> {

                final List<String> foreignKeyFields = foreignKey.getFields();
                foreignKeyFields.forEach(fieldName -> {
                    final DomainField field = domainType.findField(fieldName);
                    if (field == null)
                    {
                        throw new DomainQLException("Foreign key references non-existing '" + fieldName + "': " + this);
                    }
                });

                final DomainType type = Domain.getDomainType(foreignKey.getTargetType(), domainTypes);
                if (type == null)
                {
                    throw new DomainQLException(
                        "Foreign key references non-existing  type '" + foreignKey.getTargetType() + "': " + this);
                }

                final List<String> targetFields =  foreignKey.getTargetFields();

                if (foreignKeyFields.size() != targetFields.size())
                {
                    throw new DomainQLException("Field count mismatch in foreign key : " + foreignKey);
                }

                for (int i = 0; i < foreignKeyFields.size(); i++)
                {
                    final DomainField fieldA = domainType.findField(foreignKeyFields.get(i));
                    final DomainField fieldB = type.findField(targetFields.get(i));

                    if (fieldB == null || !fieldA.getType().equals(fieldB.getType()))
                    {
                        throw new DomainQLException("Field type mismatch: " + foreignKey);
                    }
                }
            });

        }
    }
}
