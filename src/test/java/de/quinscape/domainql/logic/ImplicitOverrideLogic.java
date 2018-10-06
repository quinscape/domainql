package de.quinscape.domainql.logic;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.SourceOneInput;

@GraphQLLogic
public class ImplicitOverrideLogic
{
    @GraphQLQuery
    public boolean queryThatOverrides(SourceOneInput sourceOneInput)
    {
        return true;
    }
}
