package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;

@GraphQLLogic
public class ImplicitOverrideNonInputLogic
{
    @GraphQLQuery
    public boolean queryThatOverrides(SourceOne sourceOneInput)
    {
        return true;
    }
}
