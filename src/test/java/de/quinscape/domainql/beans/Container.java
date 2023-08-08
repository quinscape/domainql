package de.quinscape.domainql.beans;

import jakarta.validation.constraints.NotNull;

/**
 * Generic container test bean for direct inclusion of the generic T
 * 
 * @param <T>
 */
public class Container<T>
{
    private T value;

    private int num;


    public T getValue()
    {
        return value;
    }


    public void setValue(T value)
    {
        this.value = value;
    }


    @NotNull
    public int getNum()
    {
        return num;
    }


    public void setNum(int num)
    {
        this.num = num;
    }
}
