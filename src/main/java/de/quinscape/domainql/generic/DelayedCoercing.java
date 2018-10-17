package de.quinscape.domainql.generic;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

public class DelayedCoercing<I, O>
    implements Coercing<I, O>
{
    private volatile Coercing<I, O> target;


    public void setTarget(Coercing<I, O> coercing)
    {
        this.target = coercing;
    }

    private void ensureInitialized()
    {
        if (target == null)
        {
            throw new IllegalStateException("DelayedCoercing not initialized");
        }
    }


    @Override
    public O serialize(Object dataFetcherResult) throws CoercingSerializeException
    {
        ensureInitialized();
        return target.serialize(dataFetcherResult);
    }


    @Override
    public I parseValue(Object input) throws CoercingParseValueException
    {
        ensureInitialized();
        return target.parseValue(input);
    }


    @Override
    public I parseLiteral(Object input) throws CoercingParseLiteralException
    {
        ensureInitialized();
        return target.parseLiteral(input);
    }
}
