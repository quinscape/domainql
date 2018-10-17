package de.quinscape.domainql.scalar;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;

/**
 * GraphQL Scalar implementation for java.sql.Timestamp.
 */
public class GraphQLTimestampScalar
    extends graphql.schema.GraphQLScalarType
{
    public GraphQLTimestampScalar()
    {
        super("Timestamp", "SQL timestamp equivalent", new Coercing<Timestamp, String>(){
            @Override
            public String serialize(Object dataFetcherResult) throws CoercingSerializeException
            {
                if (dataFetcherResult instanceof Timestamp)
                {
                    try
                    {
                        return toISO8601((Timestamp) dataFetcherResult);
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
            public Timestamp parseValue(Object input) throws CoercingParseValueException
            {
                if (!(input instanceof String))
                {
                    throw new CoercingParseValueException("Cannot coerce " + input + " to Timestamp");
                }


                final String isoString = (String) input;
                return convert(isoString);
            }


            @Override
            public Timestamp parseLiteral(Object input) throws CoercingParseLiteralException
            {
                if (!(input instanceof StringValue))
                {
                    throw new CoercingParseValueException("Cannot coerce " + input + " to Timestamp");
                }

                return convert(((StringValue) input).getValue());
            }
        });
    }


    public static String toISO8601(Timestamp dataFetcherResult)
    {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(tz);

        return df.format(dataFetcherResult);
    }


    public static Timestamp convert(String isoString)
    {
        Instant instant = Instant.parse(isoString);
        return new Timestamp(instant.toEpochMilli());
    }
}
