package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.annotation.ResolvedGenericType;
import de.quinscape.domainql.beans.AnnotatedPayload;
import de.quinscape.domainql.util.Paged;

import java.util.ArrayList;
import java.util.List;

@GraphQLLogic
public class DegenerifyAndRenameLogic
{

    @GraphQLQuery
    @ResolvedGenericType("PagedAndRenamed")
    public Paged<AnnotatedPayload> getPayload()
    {
        final Paged<AnnotatedPayload> paged = new Paged<>();

        List<AnnotatedPayload> rows = new ArrayList<>();

        rows.add(new AnnotatedPayload("aaa", 555));
        rows.add(new AnnotatedPayload("bbb", 7777));

        paged.setRows(rows);
        paged.setRowCount(2);

        return paged;
    }

}
