package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.SourceOne;

@GraphQLLogic
public class ImplicitOverrideNonInputLogic
{
    @GraphQLQuery
    public boolean queryThatOverrides(SourceOne sourceOneInput)
    {
        return true;
    }
}
