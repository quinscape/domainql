package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.annotation.GraphQLQuery;

@GraphQLLogic
public class MinimalLogic
{
    @GraphQLQuery
    public boolean query()
    {
        return true;
    }

    @GraphQLMutation
    public boolean mutation()
    {
        return false;
    }
}
