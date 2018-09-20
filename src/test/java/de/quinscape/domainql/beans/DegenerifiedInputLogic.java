package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLField;
import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.util.Paged;

@GraphQLLogic
public class DegenerifiedInputLogic
{

    // Using "Paged" as input for convenience, not because it makes a lot of sense to submit paged data
    @GraphQLMutation
    public String mutationWithDegenerifiedInput(

        @GraphQLField(notNull = true)
        Paged<Payload> pagedPayload

    )
    {
        StringBuilder buff = new StringBuilder();

        for (Payload payload : pagedPayload.getRows())
        {
            buff.append(payload.getName())
                .append(":")
                .append(payload.getNum())
                .append("|");
        }

        return buff.toString();
    }
}
