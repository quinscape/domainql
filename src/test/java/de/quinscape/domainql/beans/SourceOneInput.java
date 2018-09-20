package de.quinscape.domainql.beans;

import de.quinscape.domainql.testdomain.tables.pojos.SourceOne;

public class SourceOneInput
    extends SourceOne
{
    private String extra;


    public String getExtra()
    {
        return extra;
    }


    public void setExtra(String extra)
    {
        this.extra = extra;
    }
}
