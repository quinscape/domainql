package de.quinscape.domainql.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.jooq.DSLContext;
import org.jooq.Table;

import java.util.List;

public class JOOQFetcher
    implements DataFetcher<List<?>>
{
    private final DSLContext dslContext;

    private final Table<?> table;


    public JOOQFetcher(DSLContext dslContext, Table<?> table)
    {
        this.dslContext = dslContext;
        this.table = table;
    }


    @Override
    public List<?> get(DataFetchingEnvironment environment)
    {
        

        return null;
    }
}
