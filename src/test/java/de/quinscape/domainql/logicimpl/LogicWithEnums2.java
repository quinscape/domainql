package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.beans.AnotherEnum;
import de.quinscape.domainql.beans.BeanWithEnum;
import de.quinscape.domainql.beans.MyEnum;

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
