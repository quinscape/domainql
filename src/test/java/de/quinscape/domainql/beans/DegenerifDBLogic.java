package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.testdomain.tables.pojos.SourceOne;
import de.quinscape.domainql.util.Paged;

import java.util.Collections;

@GraphQLLogic
public class DegenerifDBLogic
{
    @GraphQLQuery
    public Paged<SourceOne> sourceOnes()
    {
        return new Paged<>(Collections.emptyList(),0);
    }
}
