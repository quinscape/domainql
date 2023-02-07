package de.quinscape.domainql.jsonb;

import de.quinscape.domainql.scalar.BigIntegerScalar;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import java.math.BigInteger;
import java.util.Map;

/**
 * GraphQL Scalar equivalent for a Postgresql jsonb field.
 */
public final class JSONBScalar
{
    private JSONBScalar()
    {
        // no instances
    }


    public static GraphQLScalarType newScalar()
    {
        return GraphQLScalarType.newScalar()
            .name("JSONB")
            .description("Postgresql jsonb type equivalent")
            .coercing(new BigIntegerScalar.Coercing())
            .build();
    }


    public static class Coercing
        implements graphql.schema.Coercing<JSONB, Map<String, Object>>
    {

        @Override
        public Map<String, Object> serialize(Object dataFetcherResult) throws CoercingSerializeException
        {
            if (dataFetcherResult == null)
            {
                return null;
            }
            else if (dataFetcherResult instanceof JSONB)
            {
                return ((JSONB) dataFetcherResult).asMap();
            }
            else
            {
                throw new CoercingSerializeException("Could not convert " + dataFetcherResult + " to ISO string");
            }
        }


        @Override
        public JSONB parseValue(Object input) throws CoercingParseValueException
        {
            if (input == null)
            {
                return null;
            }
            else if (input instanceof String)
            {
                return JSONB.forValue((String) input);
            }
            else if (input instanceof Map)
            {
                return new JSONB((Map<String, Object>) input);
            }
            throw new CoercingParseValueException("Cannot convert " + input + " to JSONB");
        }


        @Override
        public JSONB parseLiteral(Object input) throws CoercingParseLiteralException
        {
            throw new CoercingParseValueException("Cannot coerce literal to JSONB");
        }
    }
}
