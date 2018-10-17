package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.Container;
import de.quinscape.domainql.beans.Payload;

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
