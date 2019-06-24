package de.quinscape.domainql.docs;

public class ParamDoc
{
    private String name;

    private String description;


    public ParamDoc()
    {
        this(null, null);
    }
    
    public ParamDoc(String name, String description)
    {
        setName(name);
        setDescription(description);
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
}
