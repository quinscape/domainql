package de.quinscape.domainql.fetcher;

import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.TableField;

import java.util.List;

/**
 * Fetches embedded foreign key references.
 */
public class ReferenceFetcher
    implements DataFetcher<Object>
{
    private final DSLContext dslContext;

    private final String jsonName;

    private final Table<?> table;

    private final Class<?> pojoType;

    public ReferenceFetcher(DSLContext dslContext, String jsonName, Table<?> table, Class<?> pojoType)
    {
        this.dslContext = dslContext;
        this.jsonName = jsonName;
        this.table = table;
        this.pojoType = pojoType;
    }

    @Override
    public Object get(DataFetchingEnvironment environment)
    {
        final Object id = JSONUtil.DEFAULT_UTIL.getProperty(environment.getSource(), jsonName);

        final List<? extends TableField<?, ?>> pkFields = table.getPrimaryKey().getFields();
        // XXX: support multi-field keys
        if (pkFields.size() != 1)
        {
            throw new UnsupportedOperationException("Multi-key references not implented yet: Cannot query " + table);
        }

        final TableField<?, Object> pkField = (TableField<?, Object>) pkFields.get(0);

        return dslContext.select().from(table).where(pkField.eq(id)).fetchSingleInto(pojoType);
    }


    public String getJsonName()
    {
        return jsonName;
    }


    public Table<?> getTable()
    {
        return table;
    }


    public Class<?> getPojoType()
    {
        return pojoType;
    }
}
