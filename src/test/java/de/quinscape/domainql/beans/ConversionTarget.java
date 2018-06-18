package de.quinscape.domainql.beans;

import java.sql.Timestamp;

public class ConversionTarget
{
    private String name;
    private Timestamp created;


    public void setName(String name)
    {
        this.name = name;
    }


    public void setCreated(Timestamp created)
    {
        this.created = created;
    }


    public String getName()
    {
        return name;
    }


    public Timestamp getCreated()
    {
        return created;
    }
}
