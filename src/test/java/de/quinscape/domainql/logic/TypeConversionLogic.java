package de.quinscape.domainql.logic;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.beans.ConversionTarget;

@GraphQLLogic
public class TypeConversionLogic
{
    @GraphQLMutation
    public String mutateConverted(ConversionTarget target)
    {
        return target.getName() + ":" + target.getCreated();
    }
}
