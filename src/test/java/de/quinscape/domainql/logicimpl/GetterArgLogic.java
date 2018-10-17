package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.GetterArgBean;

@GraphQLLogic
public class GetterArgLogic
{
    @GraphQLQuery
    public GetterArgBean getterArgBean()
    {
        return new GetterArgBean("Value From GetterArgLogic");
    }
}
