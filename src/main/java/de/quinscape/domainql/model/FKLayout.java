package de.quinscape.domainql.model;

import org.svenson.JSONParameter;

public class FKLayout
{
    private final double x, y;

    public FKLayout(
        @JSONParameter("x")
        double x,
        @JSONParameter("y")
        double y
    )
    {
        this.x = x;
        this.y = y;
    }


    public double getX()
    {
        return x;
    }


    public double getY()
    {
        return y;
    }
}
