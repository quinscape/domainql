package de.quinscape.domainql.model;

import org.svenson.JSONParameter;

public class FKLayout
{
    private final float x, y;

    public FKLayout(
        @JSONParameter("x")
        float x,
        @JSONParameter("y")
        float y
    )
    {
        this.x = x;
        this.y = y;
    }


    public float getX()
    {
        return x;
    }


    public float getY()
    {
        return y;
    }
}
