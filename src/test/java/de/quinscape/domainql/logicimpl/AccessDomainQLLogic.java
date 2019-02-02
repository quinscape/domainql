package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.logic.DomainQLDataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironment;

@GraphQLLogic
public class AccessDomainQLLogic
{
    @GraphQLQuery
    public boolean accessDomainQLLogic(
        DomainQLDataFetchingEnvironment environment
    )
    {
        return environment.getDomainQL() != null;
    }
}
