package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;

@GraphQLLogic
public class TypeConversionLogic
{
    @GraphQLMutation
    public String mutateConverted(ConversionTarget target)
    {
        return target.getName() + ":" + target.getCreated();
    }
}
