package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.TestParamType;

@GraphQLLogic
public class CustomParameterProviderLogic
{

    @GraphQLQuery
    public String withCPP(TestParamType paramType)
    {
        return paramType.toString();
    }
}
