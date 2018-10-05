package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;

@GraphQLLogic
public class ImplicitOverrideLogic
{
    @GraphQLQuery
    public boolean queryThatOverrides(SourceOneInput sourceOneInput)
    {
        return true;
    }
}
