package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLField;

public class ComplexInput
{
    private String value;
    private int num;

    @GraphQLField(required = true)
    public String getValue()
    {
        return value;
    }


    public void setValue(String value)
    {
        this.value = value;
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
