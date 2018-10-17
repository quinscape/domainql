package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.testdomain.tables.SourceOne;

/* BOOM */

@GraphQLLogic
public class LogicWithWrongInjection
{
    @GraphQLQuery
    public boolean wrong(SourceOne sourceOne)
    {
        return true;
    }
}
