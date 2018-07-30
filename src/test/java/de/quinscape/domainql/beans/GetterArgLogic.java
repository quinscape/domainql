package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;

@GraphQLLogic
public class GetterArgLogic
{
    @GraphQLQuery
    public GetterArgBean getterArgBean()
    {
        return new GetterArgBean("Value From GetterArgLogic");
    }
}
