package de.quinscape.domainql.fetcher;

import de.quinscape.domainql.config.RelationModel;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Fetches embedded foreign key references or returns a value from a previously prepared {@link FetcherContext}
 */
public class ReferenceFetcher
    implements DataFetcher<Object>
{
    private final static Logger log = LoggerFactory.getLogger(ReferenceFetcher.class);


    private final DSLContext dslContext;

    /**
     * GraphQL field name of the embedded object in the parent type
     */
    private final String fieldName;

    private final RelationModel relationModel;


    public ReferenceFetcher(
        DSLContext dslContext,
        String fieldName,
        RelationModel relationModel
    )
    {
        this.dslContext = dslContext;
        this.fieldName = fieldName;
        this.relationModel = relationModel;
        if (log.isDebugEnabled())
        {
            log.debug("ReferenceFetcher: {}, {}", fieldName, relationModel);
        }
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


        final List<String> sourceFields = relationModel.getSourceFields();
        final List<? extends TableField<?, ?>> targetDBFields = relationModel.getTargetDBFields();

        final Condition condition;
        if (sourceFields.size() == 1)
        {
            final Object id = JSONUtil.DEFAULT_UTIL.getProperty(source, sourceFields.get(0));
            condition = ((Field<Object>) targetDBFields.get(0)).eq(id);

        }
        else
        {
            List<Condition> conditions = new ArrayList<>(sourceFields.size());
            for (int i = 0; i < targetDBFields.size(); i++)
            {
                final String sourceField = sourceFields.get(i);
                final TableField<?, Object> targetDBField = (TableField<?, Object>) targetDBFields.get(i);

                final Object id = JSONUtil.DEFAULT_UTIL.getProperty(source, sourceField);
                conditions.add(
                    targetDBField.eq(id)
                );
            }

            condition = DSL.and(
                conditions
            );
        }

        return dslContext.select()
            .from(relationModel.getTargetTable())
            .where(condition)
            .fetchSingleInto(relationModel.getTargetPojoClass());
    }


    public RelationModel getRelationModel()
    {
        return relationModel;
    }

}
