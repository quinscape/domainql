package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;

import java.util.Collections;
import java.util.List;

@GraphQLLogic
public class LogicWithGenerics
{
    @GraphQLMutation
    public List<Integer> mutationReturningListOfInts()
    {
        return Collections.emptyList();
    }

    @GraphQLMutation
    public boolean mutationWithIntListParam(List<Integer> args)
    {
        return true;
    }

    @GraphQLMutation
    public List<DependencyBean> mutationReturningListOfObject()
    {
        return Collections.emptyList();
    }

    @GraphQLMutation
    public boolean mutationWithObjectListParam(List<DependencyBean> args)
    {
        return true;
    }
}
