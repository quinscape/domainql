package de.quinscape.domainql.docs;

import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FieldDoc
{
    private String name;

    private String description;

    private List<ParamDoc> paramDocs;

    public FieldDoc()
    {
        this(null, null);
    }
    
    public FieldDoc(String name, String description)
    {
        this.name = name;
        this.description = description;
    }


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    public List<ParamDoc> getParamDocs()
    {
        if (paramDocs == null)
        {
            return Collections.emptyList();
        }


        return paramDocs;
    }


    public void setParamDocs(List<ParamDoc> paramDocs)
    {
        this.paramDocs = paramDocs;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            ;
    }
}
