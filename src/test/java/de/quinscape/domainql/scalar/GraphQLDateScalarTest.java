package de.quinscape.domainql.scalar;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class GraphQLDateScalarTest
{

    private final static Logger log = LoggerFactory.getLogger(GraphQLDateScalarTest.class);

    @Test
    public void testConversion()
    {
        final GraphQLDateScalar scalar = new GraphQLDateScalar();
        final Date date = (Date) scalar.getCoercing().parseValue("2020-09-16T00:00:00.000Z");
        final Date date2 = (Date) scalar.getCoercing().parseValue("2020-09-16");

        assertThat(date.toString(), is("2020-09-16"));
        assertThat(date2.toString(), is("2020-09-16"));

    }
}
