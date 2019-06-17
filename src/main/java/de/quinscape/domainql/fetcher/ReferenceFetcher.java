package de.quinscape.domainql.fetcher;

import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.TableField;

import java.util.List;

/**
 * Fetches embedded foreign key references or returns a value from a previously prepared {@link FetcherContext}
 */
public class ReferenceFetcher
    implements DataFetcher<Object>
{
    private final DSLContext dslContext;

    /**
     * GraphQL field name of the embedded object in the parent type
     */
    private final String fieldName;

    /**
     * Name of the referencing id field.
     */
    private final String idFieldName;

    private final Table<?> table;

    private final Class<?> pojoType;

    public ReferenceFetcher(
        DSLContext dslContext,
        String fieldName,
        String idFieldName,
        Table<?> table,
        Class<?> pojoType
    )
    {
        this.dslContext = dslContext;
        this.fieldName = fieldName;
        this.idFieldName = idFieldName;
        this.table = table;
        this.pojoType = pojoType;
    }

    @Override
    public Object get(DataFetchingEnvironment environment)
    {
        final Object source = environment.getSource();

        FetcherContext fetcherContext;
        if (source instanceof DomainObject && (fetcherContext = ((DomainObject) source).lookupFetcherContext()) != null)
        {
            return fetcherContext.getProperty(fieldName);
        }

        final Object id = JSONUtil.DEFAULT_UTIL.getProperty(source, idFieldName);

        final List<? extends TableField<?, ?>> pkFields = table.getPrimaryKey().getFields();
        // XXX: support multi-field keys
        if (pkFields.size() != 1)
        {
            throw new UnsupportedOperationException("Multi-key references not implented yet: Cannot query " + table);
        }

        final TableField<?, Object> pkField = (TableField<?, Object>) pkFields.get(0);

        return dslContext.select().from(table).where(pkField.eq(id)).fetchSingleInto(pojoType);
    }


    /**
     * Returns the name of the id field in the referencing / source type.
     *
     * @return  id field name
     */
    public String getIdFieldName()
    {
        return idFieldName;
    }


    /**
     * Returns the JOOQ table for the referenced / target type.
     * @return
     */
    public Table<?> getTable()
    {
        return table;
    }


    /**
     * Returns the pojo type of the referenced / target type
     * @return
     */
    public Class<?> getPojoType()
    {
        return pojoType;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "dslContext = " + dslContext
            + ", idFieldName = '" + idFieldName + '\''
            + ", table = " + table
            + ", pojoType = " + pojoType
            ;
    }
}
