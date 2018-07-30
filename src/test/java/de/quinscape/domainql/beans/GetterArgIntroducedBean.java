package de.quinscape.domainql.beans;

/**
 * Yet unknown bean introduced by a getter arg method
 */
public class GetterArgIntroducedBean
{
    private final String name;


    public GetterArgIntroducedBean(String name)
    {
        this.name = name;
    }


    public String getName()
    {
        return name;
    }
}
