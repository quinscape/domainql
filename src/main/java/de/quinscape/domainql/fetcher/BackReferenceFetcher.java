package de.quinscape.domainql.fetcher;

import de.quinscape.domainql.config.RelationModel;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;

/**
 * Fetches single to multiple objects via a foreign key back-reference.
 */
public class BackReferenceFetcher
    implements DataFetcher<Object>
{
    private final DSLContext dslContext;

    private final RelationModel relationModel;

    public BackReferenceFetcher(DSLContext dslContext, RelationModel relationModel)
    {
        this.dslContext = dslContext;
        this.relationModel = relationModel;
    }

    @Override
    public Object get(DataFetchingEnvironment environment)
    {

        FetcherContext fetcherContext;
        final Object source = environment.getSource();

        if (source instanceof DomainObject && (fetcherContext = ((DomainObject) source).lookupFetcherContext()) != null)
        {
            return fetcherContext.getProperty(relationModel.getRightSideObjectName());
        }

        final List<? extends TableField<?, ?>> targetDBFields = relationModel.getTargetDBFields();

        final List<String> targetFields = relationModel.getTargetFields();
        final List<? extends TableField<?, ?>> sourceDBFields = relationModel.getSourceDBFields();

        final Condition condition;
        if (targetDBFields.size() == 1)
        {
            final Object id = JSONUtil.DEFAULT_UTIL.getProperty(source, targetFields.get(0));
            final TableField<?, Object> fkField = (TableField<?, Object>) sourceDBFields.get(0);
            condition = fkField.eq(id);
        }
        else
        {

            List<Condition> conditions = new ArrayList<>(targetDBFields.size());

            for (int i = 0; i < targetDBFields.size(); i++)
            {
                final String targetField = targetFields.get(i);
                final TableField<?, Object> sourceDBField = (TableField<?, Object>) sourceDBFields.get(i);

                final Object id = JSONUtil.DEFAULT_UTIL.getProperty(source, targetField);
                conditions.add(
                    sourceDBField.eq(id)
                );
            }

            condition = DSL.and(conditions);
        }

        final SelectConditionStep<Record> step = dslContext.select()
            .from(relationModel.getSourceTable())
            .where(condition);
        final boolean isOneToOne = relationModel.getTargetField() == TargetField.ONE;
        final Class<?> pojoClass = relationModel.getSourcePojoClass();

        return isOneToOne ? step.fetchSingleInto(pojoClass) : step.fetchInto(
            pojoClass);
    }

    public RelationModel getRelationModel()
    {
        return relationModel;
    }

}
