package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.Container;
import de.quinscape.domainql.beans.Payload;

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
