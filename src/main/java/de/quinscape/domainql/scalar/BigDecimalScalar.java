package de.quinscape.domainql.scalar;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import java.math.BigDecimal;


/**
 * Alternative BigDecimal scalar implementation that uses string values as serialized format.
 * <p>
 * This simplifies handling of decimal values by not requiring special JSON parsing to handle the values safely.
 * Both in terms of value range as well as in terms of the IEEE float immanent numeric representation problems.
 * </p>
 */
public class BigDecimalScalar
{
    private BigDecimalScalar()
    {
        // no instances
    }

    public static GraphQLScalarType newScalar()
    {
        return GraphQLScalarType.newScalar()
            .name("BigDecimal")
            .description("BigDecimal wrapped as string")
            .coercing(new Coercing())
            .build();
    }


    public static class Coercing
        implements graphql.schema.Coercing<BigDecimal, String>
    {
        @Override
        public String serialize(Object dataFetcherResult) throws CoercingSerializeException
        {
            if (dataFetcherResult instanceof BigDecimal)
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
        public BigDecimal parseValue(Object input) throws CoercingParseValueException
        {
            if (!(input instanceof String))
            {
                throw new CoercingParseValueException("Cannot coerce " + input + " to BigDecimal");
            }

            final String stringValue = (String) input;
            return new BigDecimal(stringValue);
        }


        @Override
        public BigDecimal parseLiteral(Object input) throws CoercingParseLiteralException
        {
            if (!(input instanceof StringValue))
            {
                throw new CoercingParseValueException("Cannot coerce " + input + " to BigDecimal");
            }
            return new BigDecimal(((StringValue) input).getValue());
        }
    }
}
