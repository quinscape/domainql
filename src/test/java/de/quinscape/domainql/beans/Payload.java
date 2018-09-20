package de.quinscape.domainql.beans;

public class Payload
{
    private String name;

    private int num;

    public Payload()
    {
        this(null, 0);
    }

    public Payload(String name, int num)
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
