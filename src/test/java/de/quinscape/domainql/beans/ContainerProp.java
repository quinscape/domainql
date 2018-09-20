package de.quinscape.domainql.beans;

/**
 * Generic bean with direct generic property.
 * 
 * @param <T>
 */
public class ContainerProp<T>
{
    private Container<T> value;

    public Container<T> getValue()
    {
        return value;
    }


    public void setValue(Container<T> value)
    {
        this.value = value;
    }
}
