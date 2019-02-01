package de.quinscape.domainql.beans;

import org.svenson.JSONProperty;

public class IgnoredPropsBean
{
    private  String value;


    public String getValue()
    {
        return value;
    }


    public void setValue(String value)
    {
        this.value = value;
    }

    @JSONProperty(ignore = true)
    public String getValuePlus()
    {
        return "+" + value;
    }
}
