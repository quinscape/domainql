package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.TargetSeven;

import java.util.UUID;

@GraphQLLogic 
public class OutputTypeOverrideLogic
{
    @GraphQLQuery
    public TargetSeven targetSeven(TargetSeven targetSeven)
    {
        if (targetSeven.getId() == null)
        {
            targetSeven.setId(UUID.randomUUID().toString());
        }
        if (targetSeven.getName() == null)
        {
            targetSeven.setName("Unnamed");
        }
        return targetSeven;
    }
}
