package de.quinscape.domainql.beans;

/**
 * Class containing byte[] data;
 */
public class BinaryBean
{
    private String name;
    private byte[] data;


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public byte[] getData()
    {
        return data;
    }


    public void setData(byte[] data)
    {
        this.data = data;
    }
}
