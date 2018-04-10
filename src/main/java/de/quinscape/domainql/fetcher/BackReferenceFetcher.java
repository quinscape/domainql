package de.quinscape.domainql.fetcher;

import de.quinscape.domainql.util.JSONUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.jooq.DSLContext;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.TableField;

/**
 * Fetches single to multiple objects via a foreign key back-reference.
 */
public class BackReferenceFetcher
    implements DataFetcher<Object>
{
    private final DSLContext dslContext;

    private final String jsonName;

    private final Table<?> table;

    private final Class<?> pojoType;

    private final ForeignKey<?, ?> foreignKey;

    private final boolean oneToOne;


    public BackReferenceFetcher(DSLContext dslContext, String jsonName, Table<?> table, Class<?> pojoType, ForeignKey<?,?> foreignKey, boolean oneToOne)
    {
        this.dslContext = dslContext;
        this.jsonName = jsonName;
        this.table = table;
        this.pojoType = pojoType;
        this.foreignKey = foreignKey;
        this.oneToOne = oneToOne;
    }

    @Override
    public Object get(DataFetchingEnvironment environment)
    {
        final Object id = JSONUtil.DEFAULT_UTIL.getProperty(environment.getSource(), jsonName);

        final TableField<?, Object> fkField = (TableField<?, Object>) foreignKey.getFields().get(0);

        final SelectConditionStep<Record> step = dslContext.select().from(table).where(fkField.eq(id));
        return oneToOne ? step.fetchSingleInto(pojoType) : step.fetchInto(pojoType);
    }
}
