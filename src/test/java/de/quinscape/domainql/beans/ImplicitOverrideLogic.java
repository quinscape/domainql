package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import org.junit.Test;

@GraphQLLogic
public class ImplicitOverrideLogic
{
    @GraphQLQuery
    public boolean queryThatOverrides(SourceOneInput sourceOneInput)
    {
        return true;
    }
}
