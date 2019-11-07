package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLField;

public class ComplexInput
{
    private String value;
    private int num;


    public ComplexInput()
    {
        this(null, 0);
    }

    public ComplexInput(String value, int num)
    {
        this.value = value;
        this.num = num;
    }


    @GraphQLField(notNull = true)
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


    @Override
    public String toString()
    {
        return "ComplexInput: "
            + "value = '" + value + '\''
            + ", num = " + num
            ;
    }
}
