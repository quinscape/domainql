package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;

@GraphQLLogic
public class LogicWithEnums
{
    @GraphQLQuery
    public String queryWithEnumArg(MyEnum myEnum)
    {
        return "(" + myEnum.name() + ")";
    }

    @GraphQLQuery
    public String queryWithObjectArgWithEnum(BeanWithEnum beanWithEnum)
    {
        return beanWithEnum.toString();
    }

}
