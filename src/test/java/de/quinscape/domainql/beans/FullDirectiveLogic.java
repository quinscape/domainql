package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;

@GraphQLLogic
public class FullDirectiveLogic
{

    @GraphQLQuery( full = true)
    public FulLResponse fullQuery()
    {
        final FulLResponse response = new FulLResponse();
        response.setName("Blafusel");
        response.setNum(12948);
        
        return response;
    }
}
