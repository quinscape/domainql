package de.quinscape.domainql.beans;

import java.math.BigInteger;

public class BIContainer
{
    private String name;

    private BigInteger value;


    public BIContainer()
    {
        this(null, null);
    }


    public BIContainer(String name, BigInteger value)
    {
        this.name = name;
        this.value = value;
    }


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public BigInteger getValue()
    {
        return value;
    }


    public void setValue(BigInteger value)
    {
        this.value = value;
    }
}
