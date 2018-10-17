package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.beans.DependencyBean;

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
