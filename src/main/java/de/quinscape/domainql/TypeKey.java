package de.quinscape.domainql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class TypeKey
{
    private final static Logger log = LoggerFactory.getLogger(TypeKey.class);


    private final TypeContext ctx;


    public TypeKey(TypeContext ctx)
    {
        this.ctx = ctx;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (obj instanceof TypeKey)
        {
            TypeKey that = (TypeKey) obj;
            return Objects.equals(this.ctx, that.ctx);
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(ctx);
    }


    @Override
    public String toString()
    {
        return super.toString() + ": " + ctx;
    }
}
