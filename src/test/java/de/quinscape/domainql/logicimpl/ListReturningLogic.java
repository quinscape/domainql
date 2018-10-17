package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.testdomain.tables.pojos.SourceThree;

import java.util.Collections;
import java.util.List;

@GraphQLLogic
public class ListReturningLogic
{
    @GraphQLQuery
    public List<SourceThree> listOfThrees()
    {
        return Collections.emptyList();
    }

}
