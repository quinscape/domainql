package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;

@GraphQLLogic
public class NoMirroLogic
{
    @GraphQLQuery
    public NoMirrorBean getValue()
    {
        return null;
    }
}
