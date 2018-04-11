package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.testdomain.tables.records.SourceOneRecord;


@GraphQLLogic
public class LogicWithWrongInjection2
{
    @GraphQLQuery
    public boolean wrong(SourceOneRecord sourceOne)
    {
        return true;
    }
}
