package de.quinscape.domainql.beans;

public class BeanWithEnum
{
    private AnotherEnum anotherEnum;


    public AnotherEnum getAnotherEnum()
    {
        return anotherEnum;
    }


    public void setAnotherEnum(AnotherEnum anotherEnum)
    {
        this.anotherEnum = anotherEnum;
    }


    @Override
    public String toString()
    {
        return "BeanWithEnum: "
            + "anotherEnum = " + anotherEnum
            ;
    }
}
