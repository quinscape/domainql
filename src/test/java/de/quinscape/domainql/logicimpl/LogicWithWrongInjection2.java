package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.testdomain.tables.records.SourceOneRecord;

/* BOOM! */

@GraphQLLogic
public class LogicWithWrongInjection2
{
    @GraphQLQuery
    public boolean wrong(SourceOneRecord sourceOne)
    {
        return true;
    }
}
