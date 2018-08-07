package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;

@GraphQLLogic
public class LogicWithEnums2
{

    @GraphQLMutation
    public MyEnum enumMutation()
    {
        return MyEnum.B;
    }

    @GraphQLMutation
    public BeanWithEnum objectWithEnumMutation()
    {
        final BeanWithEnum beanWithEnum = new BeanWithEnum();
        beanWithEnum.setAnotherEnum(AnotherEnum.Z);
        return beanWithEnum;
    }
}
