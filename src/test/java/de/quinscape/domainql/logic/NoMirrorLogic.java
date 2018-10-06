package de.quinscape.domainql.logic;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.NoMirrorBean;

@GraphQLLogic
public class NoMirrorLogic
{
    @GraphQLQuery
    public NoMirrorBean getValue()
    {
        return null;
    }
}
