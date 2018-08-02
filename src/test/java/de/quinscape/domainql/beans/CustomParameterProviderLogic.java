package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;

@GraphQLLogic
public class CustomParameterProviderLogic
{

    @GraphQLQuery
    public String withCPP(TestParamType paramType)
    {
        return paramType.toString();
    }
}
