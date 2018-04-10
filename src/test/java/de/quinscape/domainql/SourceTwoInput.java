package de.quinscape.domainql;


import de.quinscape.domainql.testdomain.tables.pojos.SourceTwo;

public class SourceTwoInput
    extends SourceTwo
{
    private String foo;


    public String getFoo()
    {
        return foo;
    }


    public void setFoo(String foo)
    {
        this.foo = foo;
    }
}
