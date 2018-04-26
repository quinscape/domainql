package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;

@GraphQLLogic
public class LogicWithAnnotated
{

    @GraphQLMutation
    public AnnotatedBean annotated(AnnotatedBeanInput in)
    {
        return null;
    }
}
