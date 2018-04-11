package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;

/* BOOM */ import de.quinscape.domainql.testdomain.tables.SourceOne; /* BOOM */

@GraphQLLogic
public class LogicWithWrongInjection
{
    @GraphQLQuery
    public boolean wrong(SourceOne sourceOne)
    {
        return true;
    }
}
