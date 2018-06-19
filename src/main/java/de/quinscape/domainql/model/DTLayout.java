package de.quinscape.domainql.model;

import org.svenson.JSONParameter;

public class DTLayout
{
    private final double x, y;
    private final String color;


    public DTLayout(
        @JSONParameter("x")
        double x,
        @JSONParameter("y")
        double y,
        @JSONParameter("color")
        String color
    )
    {
        this.x = x;
        this.y = y;
        this.color = color;
    }


    public double getX()
    {
        return x;
    }


    public double getY()
    {
        return y;
    }


    public String getColor()
    {
        return color;
    }
}
