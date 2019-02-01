package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.IgnoredPropsBean;

@GraphQLLogic
public class IgnoredPropsLogic
{
    @GraphQLQuery
    public IgnoredPropsBean igPropBean()
    {
        return null;
    }
}
