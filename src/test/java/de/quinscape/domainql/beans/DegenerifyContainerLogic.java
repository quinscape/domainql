package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;

@GraphQLLogic
public class DegenerifyContainerLogic
{
    @GraphQLQuery
    public Container<Payload> queryContainer()
    {
        final Container<Payload> container = new Container<>();

        container.setValue(new Payload("DDD", 444));
        container.setNum(555);
        return container;
    }
}
