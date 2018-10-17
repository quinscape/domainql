package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.Payload;
import de.quinscape.domainql.util.Paged;

import java.util.ArrayList;
import java.util.List;

@GraphQLLogic
public class DegenerifyLogic
{
    @GraphQLQuery
    public Paged<Payload> getPayload()
    {
        final Paged<Payload> paged = new Paged<>();

        List<Payload> rows = new ArrayList<>();

        rows.add(new Payload("aaa", 5));
        rows.add(new Payload("bbb", 7));

        paged.setRows(rows);
        paged.setRowCount(2);

        return paged;
    }

}
