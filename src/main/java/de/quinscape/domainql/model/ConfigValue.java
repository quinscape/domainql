package de.quinscape.domainql.model;

public class ConfigValue
{
    private final String name;

    private final String value;


    public ConfigValue(String name, String value)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }


        if (value == null)
        {
            throw new IllegalArgumentException("value can't be null");
        }

        this.name = name;
        this.value = value;
    }


    public String getName()
    {
        return name;
    }


    public String getValue()
    {
        return value;
    }
}
