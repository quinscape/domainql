package de.quinscape.domainql.scalar;

import graphql.language.IntValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import java.math.BigInteger;

/**
 * GraphQL Scalar implementation for currency values. Stored as Long value in 1/10000th currency units. (e.g. 1.15
 * EUR = 11500 )
 */
public class CurrencyScalar
{

    private CurrencyScalar()
    {
        // no instances
    }


    public static GraphQLScalarType newScalar()
    {
        return GraphQLScalarType.newScalar()
            .name("Currency")
            .description("Currency value")
            .coercing(new Coercing())
            .build();
    }


    public static class Coercing
        implements graphql.schema.Coercing<Long, Long>
    {

        @Override
        public Long serialize(Object dataFetcherResult) throws CoercingSerializeException
        {
            if (dataFetcherResult == null)
            {
                return null;
            }

            return ((Number) dataFetcherResult).longValue();
        }


        @Override
        public Long parseValue(Object input) throws CoercingParseValueException
        {
            if (!(input instanceof Number))
            {
                throw new CoercingParseValueException("Cannot coerce " + input + " to Currency");
            }

            return ((Number) input).longValue();
        }


        @Override
        public Long parseLiteral(Object input) throws CoercingParseLiteralException
        {
            if (!(input instanceof IntValue))
            {
                throw new CoercingParseValueException("Cannot coerce " + input + " to Currency");
            }
            final BigInteger value = ((IntValue) input).getValue();
            return value.longValue();
        }
    }
}
