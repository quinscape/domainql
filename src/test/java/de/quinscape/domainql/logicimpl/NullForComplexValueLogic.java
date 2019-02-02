package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.ComplexInput;

@GraphQLLogic
public class NullForComplexValueLogic
{
    @GraphQLQuery
    public boolean logicWithComplexInput(
        ComplexInput complexInput
    )
    {
        return complexInput == null;
    }
}
