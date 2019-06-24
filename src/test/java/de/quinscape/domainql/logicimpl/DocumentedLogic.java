package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.annotation.GraphQLTypeParam;
import de.quinscape.domainql.beans.DocumentedBean;
import de.quinscape.domainql.beans.DocumentedEnum;
import de.quinscape.domainql.beans.SourceOne;
import de.quinscape.domainql.docs.TypeDoc;
import de.quinscape.domainql.testdomain.tables.pojos.Foo;
import de.quinscape.domainql.util.Paged;
import graphql.schema.DataFetchingEnvironment;

import javax.validation.constraints.NotNull;
import java.util.Collections;

/**
 * Target to be analyzed by DocsExtractorTest
 *
 * @see de.quinscape.domainql.docs.DocsExtractorTest
 */
@GraphQLLogic
public class DocumentedLogic
{
    /**
     * A minimal GraphQL query
     *
     * @return always true
     */
    @GraphQLQuery
    public boolean query()
    {
        return true;
    }

    /**
     * Another query
     */
    @GraphQLQuery(value = "anotherQuery")
    public DocumentedBean query2(DocumentedBean documentedBean)
    {
        return documentedBean;
    }

    /**
     * A GraphQL mutation
     *
     * @param foo   foo param desc
     *
     * @return always true
     */
    @GraphQLMutation
    public boolean mutation(int foo)
    {
        return false;
    }

    @GraphQLQuery
    public DocumentedEnum enumLogic()
    {
        return null;
    }

    @GraphQLQuery
    public Paged<Foo> fooPaged()
    {
        return null;
    }


    /**
     * Paginated result of type [T]
     * 
     * @param type
     * @param env
     * @param <T>
     * @return
     */
    @GraphQLQuery
    public <T> Paged<T> genericPaged(
        @GraphQLTypeParam(
            types = {
                Foo.class
            }
        )
            Class<T> type,
        DataFetchingEnvironment env
    )
    {
        return new Paged<>(Collections.emptyList(), 0);
    }
}
