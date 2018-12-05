package de.quinscape.domainql.util;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.generic.DomainObject;
import org.jooq.DSLContext;
import org.jooq.InsertQuery;
import org.jooq.StoreQuery;
import org.jooq.Table;
import org.jooq.UpdateQuery;

import static org.jooq.impl.DSL.*;

/**
 * Contains some JOOQ util methods for DomainQL domain objects.
 */
public final class DomainObjectUtil
{
    private DomainObjectUtil()
    {
        // no instances
    }


    /**
     * Inserts the given domain object
     *
     * @param dslContext   DSL context
     * @param domainQL     DomainQL instance
     * @param domainObject domain object
     *
     * @return result count for insert statement
     */
    public static int insert(DSLContext dslContext, DomainQL domainQL, DomainObject domainObject)
    {
        final String domainType = domainObject.getDomainType();
        final Table<?> jooqTable = domainQL.getJooqTable(domainType);

        final String id = (String) domainObject.getProperty("id");

        final InsertQuery<?> insertQuery = dslContext.insertQuery(
            jooqTable
        );
        insertQuery.addConditions(
            field(
                name(
                    "id"
                )
            )
                .eq(
                    id
                )
        );

        addFieldValues(insertQuery, domainObject);

        return insertQuery.execute();
    }


    /**
     * Updates the given domain object by id.
     *
     * @param dslContext   DSL context
     * @param domainQL     DomainQL instance
     * @param domainObject domain object with id
     *
     * @return result count for update statement
     */
    public static int update(DSLContext dslContext, DomainQL domainQL, DomainObject domainObject)
    {
        final String domainType = domainObject.getDomainType();
        final Table<?> jooqTable = domainQL.getJooqTable(domainType);

        final String id = (String) domainObject.getProperty("id");

        final UpdateQuery<?> updateQuery = dslContext.updateQuery(
            jooqTable
        );
        updateQuery.addConditions(
            field(
                name(
                    "id"
                )
            )
                .eq(
                    id
                )
        );

        addFieldValues(updateQuery, domainObject);

        return updateQuery.execute();
    }


    /**
     * Inserts the given domain object or does an update by id.
     * <p>
     * The method will first selectCount() the number of rows with the same id and will decided whether to insert
     * or update based on that.
     * </p>
     *
     * @param dslContext   DSL context
     * @param domainQL     DomainQL instance
     * @param domainObject domain object with id
     *
     * @return result count for update statement
     */
    public static int insertOrUpdate(DSLContext dslContext, DomainQL domainQL, DomainObject domainObject)
    {

        final String domainType = domainObject.getDomainType();

        final Table<?> jooqTable = domainQL.getJooqTable(domainType);


        final String id = (String) domainObject.getProperty("id");

        final int count = dslContext.selectCount()
            .from(jooqTable)
            .where(
                field(
                    name(
                        "id"
                    )
                )
                    .eq(id)
            )
            .execute();

        // We use the basic non-DSL JOOQ api here
        final StoreQuery<?> query;
        if (count == 0)
        {
            return insert(dslContext, domainQL, domainObject);
        }
        else
        {
            return update(dslContext, domainQL, domainObject);
        }
    }


    public static int delete(DSLContext dslContext, DomainQL domainQL, DomainObject domainObject)
    {
        final String domainType = domainObject.getDomainType();
        final String id = (String) domainObject.getProperty("id");

        return delete(dslContext, domainQL, domainType, id);
    }


    public static int delete(DSLContext dslContext, DomainQL domainQL, String domainType, String id)
    {
        final Table<?> jooqTable = domainQL.getJooqTable(domainType);

        final int count = dslContext.deleteFrom(jooqTable).where(
            field(
                name(
                    "id"
                )
            )
                .eq(id)
        ).execute();

        return count;
    }


    /**
     * Sets all domain object values in a JOOQ query.
     *
     * @param query        insert or update query
     * @param domainObject domain object
     */
    private static void addFieldValues(StoreQuery<?> query, DomainObject domainObject)
    {

        for (String name : domainObject.propertyNames())
        {
            if (name.equals(DomainObject.DOMAIN_TYPE_PROPERTY))
            {
                continue;
            }

            final Object value = domainObject.getProperty(name);

            query.addValue(
                field(name),
                value
            );
        }
    }
}
