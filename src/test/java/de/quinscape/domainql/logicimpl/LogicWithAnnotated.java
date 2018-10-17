package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.beans.AnnotatedBean;
import de.quinscape.domainql.beans.AnnotatedBeanInput;

@GraphQLLogic
public class LogicWithAnnotated
{

    @GraphQLMutation
    public AnnotatedBean annotated(AnnotatedBeanInput in)
    {
        return null;
    }
}
