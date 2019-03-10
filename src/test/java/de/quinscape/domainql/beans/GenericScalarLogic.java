package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.generic.GenericScalar;
import de.quinscape.domainql.scalar.GraphQLTimestampScalar;
import graphql.Scalars;

import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;

@GraphQLLogic
public class GenericScalarLogic
{
    @GraphQLMutation
    public GenericScalar genericScalarLogic(GenericScalar value)
    {
        if (value.getType().equals(Scalars.GraphQLInt.getName()))
        {
            return new GenericScalar(Scalars.GraphQLInt.getName(), ((int)value.getValue()) + 1);
        }
        else if (value.getType().equals(GraphQLTimestampScalar.NAME))
        {
            final Timestamp timestamp = (Timestamp)value.getValue();
            final Timestamp hourLater = new Timestamp(timestamp.toInstant().plus(1, ChronoUnit.HOURS).toEpochMilli());
            return new GenericScalar(GraphQLTimestampScalar.NAME,hourLater);
        }
        else
        {
            return null;
        }
    }


    /**
     * The types wrapped in a GenericScalar need to be used somewhere in GraphQL domain. For the purpose of the test
     * we artificially add them there, this should not be necessary in a normal domain.
     */
    @GraphQLQuery
    public int queryThatRegistersOurGenericScalarWrappedTypes(Timestamp timestamp)
    {
        return 0;
    }
}
