package de.quinscape.domainql.model;

import org.svenson.JSONParameter;

public class DTLayout
{
    private final float x, y;
    private final String color;


    public DTLayout(
        @JSONParameter("x")
        float x,
        @JSONParameter("y")
        float y,
        @JSONParameter("color")
        String color
    )
    {
        this.x = x;
        this.y = y;
        this.color = color;
    }


    public float getX()
    {
        return x;
    }


    public float getY()
    {
        return y;
    }


    public String getColor()
    {
        return color;
    }
}
