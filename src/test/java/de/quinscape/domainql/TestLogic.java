package de.quinscape.domainql;

import de.quinscape.domainql.annotation.GraphQLInput;
import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.annotation.GraphQLQuery;

import java.sql.Timestamp;

@GraphQLLogic
public class TestLogic
{
    @GraphQLQuery
    public boolean queryTruth()
    {
        return true;
    }

    @GraphQLQuery
    public String queryString(String value)
    {
        return "VALUE:" + value;
    }

    @GraphQLQuery
    public String queryString2(@GraphQLInput(required = true) String value, @GraphQLInput(required = true) String second)
    {
        return value + ":" + second;
    }

    @GraphQLQuery
    public int queryInt(int value)
    {
        return value * 2;
    }

    @GraphQLQuery
    public Timestamp queryTimestamp()
    {
        return new Timestamp(System.currentTimeMillis());
    }

    @GraphQLQuery
    public boolean queryWithComplexInput(ComplexInput complexInput)
    {
        return false;
    }

    @GraphQLMutation
    public boolean mutateString(String value)
    {
        return true;
    }
}
