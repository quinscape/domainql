package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;

@GraphQLLogic
public class DegenerifiedContainerLogic
{
    @GraphQLQuery

    public String containerQuery(Container<Payload> container)
    {
        final Payload payload = container.getValue();
        return payload.getName() + ":" + payload.getNum() + ":" + container.getNum();
    }
}
