package de.quinscape.domainql.beans;


import de.quinscape.domainql.TestFetcher;
import de.quinscape.domainql.annotation.GraphQLFetcher;

public class BeanWithFetcher
{
    private String value;


    @GraphQLFetcher(value = TestFetcher.class, data = "test")
    public String getValue()
    {
        return value;
    }


    public void setValue(String value)
    {
        this.value = value;
    }
}
