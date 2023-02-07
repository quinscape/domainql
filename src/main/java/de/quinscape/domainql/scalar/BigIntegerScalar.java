package de.quinscape.domainql.scalar;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import java.math.BigInteger;

/**
 * Alternative BigDecimal scalar implementation that uses string values as serialized format.
 * <p>
 * This simplifies handling of decimal values by not requiring special JSON parsing to handle the values safely in
 * terms of value range.
 * </p>
 */
public class BigIntegerScalar
{

    private BigIntegerScalar()
    {
        // no instances
    }


    public static GraphQLScalarType newScalar()
    {
        return GraphQLScalarType.newScalar()
            .name("BigInteger")
            .description("BigInteger wrapped as string")
            .coercing(new Coercing())
            .build();
    }


    public static class Coercing
        implements graphql.schema.Coercing<BigInteger, String>
    {
        @Override
        public String serialize(Object dataFetcherResult) throws CoercingSerializeException
        {
            if (dataFetcherResult instanceof BigInteger)
            {
                try
                {
                    return dataFetcherResult.toString();
                }
                catch (RuntimeException e)
                {
                    throw new CoercingSerializeException("Error converting " + dataFetcherResult + " to ISO string", e);
                }
            }
            else
            {
                throw new CoercingSerializeException("Could not convert " + dataFetcherResult + " to ISO string");
            }
        }


        @Override
        public BigInteger parseValue(Object input) throws CoercingParseValueException
        {
            if (!(input instanceof String))
            {
                throw new CoercingParseValueException("Cannot coerce " + input + " to BigInteger");
            }

            final String stringValue = (String) input;
            return new BigInteger(stringValue);
        }


        @Override
        public BigInteger parseLiteral(Object input) throws CoercingParseLiteralException
        {
            if (!(input instanceof StringValue))
            {
                throw new CoercingParseValueException("Cannot coerce " + input + " to BigInteger");
            }
            return new BigInteger(((StringValue) input).getValue());
        }
    }
}
