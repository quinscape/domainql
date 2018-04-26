package de.quinscape.domainql.scalar;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

/**
 * GraphQL Scalar implementation for currency values. Stored as Long value in 1/10000th currency units. (e.g. 1.15 EUR = 11500 )
 */
public class GraphQLCurrencyScalar
    extends GraphQLScalarType
{
    public GraphQLCurrencyScalar()
    {
        super("Currency", "Currency value", new Coercing<Long, String>(){
            @Override
            public String serialize(Object dataFetcherResult) throws CoercingSerializeException
            {
                if (dataFetcherResult == null)
                {
                    return  null;
                }

                long value = ((Number)dataFetcherResult).longValue();
                return String.valueOf(value);
            }


            @Override
            public Long parseValue(Object input) throws CoercingParseValueException
            {
                if (!(input instanceof String))
                {
                    throw new CoercingParseValueException("Cannot coerce " + input + " to Currency");
                }

                return Long.parseLong((String) input);
            }

            @Override
            public Long parseLiteral(Object input) throws CoercingParseLiteralException
            {
                if (!(input instanceof StringValue))
                {
                    throw new CoercingParseValueException("Cannot coerce " + input + " to Currency");
                }
                return Long.parseLong(((StringValue) input).getValue());
            }
        });
    }
}
