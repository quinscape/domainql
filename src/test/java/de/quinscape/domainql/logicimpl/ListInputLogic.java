package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.beans.ComplexInput;
import de.quinscape.domainql.beans.MyEnum;
import de.quinscape.domainql.generic.DomainObject;

import javax.validation.constraints.NotNull;
import java.util.List;

@GraphQLLogic
public class ListInputLogic
{
    @GraphQLMutation
    public String testListOfDomainObjectScalars(
        @NotNull List<DomainObject> domainObjects
        )
    {
        return domainObjects.toString();
    }

    @GraphQLMutation
    public String testListOfDomainObjects(
        @NotNull List<ComplexInput> complexInputs
        )
    {
        StringBuilder sb = new StringBuilder();

        for (ComplexInput complexInput : complexInputs)
        {
            sb.append(complexInput).append("|");
        }
        return sb.toString();
    }

    @GraphQLMutation
    public String testListOfEnums(
        @NotNull List<MyEnum> enums
        )
    {
        StringBuilder sb = new StringBuilder();

        for (MyEnum myEnum : enums)
        {
            sb.append(myEnum).append("|");
        }
        return sb.toString();
    }
}
