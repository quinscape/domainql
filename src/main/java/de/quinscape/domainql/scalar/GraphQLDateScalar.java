package de.quinscape.domainql.scalar;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * GraphQL Scalar implementation for java.sql.Date.
 */
public class GraphQLDateScalar
    extends GraphQLScalarType
{
    public GraphQLDateScalar()
    {
        super("Date", "SQL date equivalent", new Coercing<Date, String>(){
            @Override
            public String serialize(Object dataFetcherResult) throws CoercingSerializeException
            {
                if (dataFetcherResult instanceof Date)
                {
                    try
                    {

                        TimeZone tz = TimeZone.getTimeZone("UTC");
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        df.setTimeZone(tz);

                        return df.format(dataFetcherResult);
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
            public Date parseValue(Object input) throws CoercingParseValueException
            {
                if (!(input instanceof String))
                {
                    throw new CoercingParseValueException("Cannot coerce " + input + " to Date");
                }

                final String isoString = (String) input;
                return convert(isoString);
            }


            @Override
            public Date parseLiteral(Object input) throws CoercingParseLiteralException
            {
                if (!(input instanceof StringValue))
                {
                    throw new CoercingParseValueException("Cannot coerce " + input + " to Date");
                }
                return convert(((StringValue) input).getValue());
            }
        });
    }

    private final static long MILLIS_PER_DAY = TimeUnit.DAYS.toMillis(1);

    private static Date convert(String isoString)
    {
        LocalDate data = LocalDate.parse(isoString);
        return new Date(data.toEpochDay() * MILLIS_PER_DAY);
    }
}
