package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.generic.GenericDomainObject;
import de.quinscape.domainql.generic.GenericScalar;
import de.quinscape.domainql.scalar.GraphQLTimestampScalar;
import graphql.Scalars;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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

    @GraphQLQuery
    public GenericScalar genericList(GenericScalar input)
    {
        if (input != null && input.getType().equals("[Int]"))
        {
            List<Integer> l = (List<Integer>) input.getValue();
            List<Integer> out = new ArrayList<>(l.size());
            for (int i = 0; i < l.size(); i++)
            {
                out.add(l.get(i) * 3);
            }
            return new GenericScalar("[Int]", out);
        }
        return new GenericScalar("[Int]", Arrays.asList(0,3,6));
    }

    @GraphQLQuery
    public GenericScalar deserializeObjs(DomainObject in, Timestamp time)
    {
        final Timestamp now = time != null ? time : Timestamp.from(Instant.now());
        final GenericDomainObject obj = new GenericDomainObject();
        obj.setProperty(DomainObject.DOMAIN_TYPE_PROPERTY, "Foo");
        obj.setProperty("id", UUID.randomUUID().toString());
        obj.setProperty("name", "Foo #1");
        obj.setProperty("num", 1001);
        obj.setProperty("created", now);

        final GenericDomainObject obj2 = new GenericDomainObject();
        obj2.setProperty(DomainObject.DOMAIN_TYPE_PROPERTY, "Foo");
        obj2.setProperty("id", UUID.randomUUID().toString());
        obj2.setProperty("name", "Foo #2");
        obj2.setProperty("num", 1002);
        obj2.setProperty("created", now);

        return new GenericScalar("[DomainObject]", Arrays.asList(obj, obj2));
    }

    @GraphQLQuery
    public GenericScalar nullScalar(GenericScalar in)
    {
        if (in.getValue() != null)
        {
            throw new IllegalArgumentException("value must be null");
        }

        return new GenericScalar(in.getType(), null);

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
