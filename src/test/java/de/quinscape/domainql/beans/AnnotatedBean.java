package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLField;

public class AnnotatedBean
{
    private long value;


    @GraphQLField( type = "Currency")
    public long getValue()
    {
        return value;
    }


    public void setValue(long value)
    {
        this.value = value;
    }
}
