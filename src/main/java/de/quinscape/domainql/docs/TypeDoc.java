package de.quinscape.domainql.docs;

import org.svenson.JSONTypeHint;

import java.util.ArrayList;
import java.util.List;

public class TypeDoc
    implements Cloneable
{
    public final static String QUERY_TYPE = "QueryType";

    public final static String MUTATION_TYPE = "MutationType";

    private String name;

    private String description;

    private List<FieldDoc> fieldDocs = new ArrayList<>();


    public TypeDoc()
    {
        this(null);
    }


    public TypeDoc(String name)
    {
        setName(name);
    }


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public List<FieldDoc> getFieldDocs()
    {
        return fieldDocs;
    }


    @JSONTypeHint(FieldDoc.class)
    public void setFieldDocs(List<FieldDoc> fieldDocs)
    {
        if (fieldDocs == null)
        {
            throw new IllegalArgumentException("fieldDocs can't be null");
        }

        this.fieldDocs = fieldDocs;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            ;
    }
}

