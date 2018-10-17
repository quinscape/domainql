package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.BeanWithSix;

@GraphQLLogic
public class TypeRepeatLogic
{
    @GraphQLQuery
    public BeanWithSix beanWithSix()
    {
        return null;
    }
}
