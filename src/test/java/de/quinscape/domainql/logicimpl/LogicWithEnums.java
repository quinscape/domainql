package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.BeanWithEnum;
import de.quinscape.domainql.beans.MyEnum;

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
