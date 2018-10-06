package de.quinscape.domainql.logic;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.FullResponse;

@GraphQLLogic
public class FullDirectiveLogic
{

    @GraphQLQuery( full = true)
    public FullResponse fullQuery()
    {
        final FullResponse response = new FullResponse();
        response.setName("Blafusel");
        response.setNum(12948);
        
        return response;
    }
}
