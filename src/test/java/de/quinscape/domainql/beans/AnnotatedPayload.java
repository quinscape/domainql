package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLObject;

public class AnnotatedPayload
{
    private String name;

    private int num;

    public AnnotatedPayload()
    {
        this(null, 0);
    }

    public AnnotatedPayload(String name, int num)
    {

        this.name = name;
        this.num = num;
    }


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public int getNum()
    {
        return num;
    }


    public void setNum(int num)
    {
        this.num = num;
    }

}
