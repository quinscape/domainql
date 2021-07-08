package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.annotation.GraphQLTypeParam;
import de.quinscape.domainql.beans.Container;
import de.quinscape.domainql.beans.TargetSeven;
import graphql.schema.DataFetchingEnvironment;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@GraphQLLogic 
public class OutputTypeOverrideByParamLogic
{

    @GraphQLQuery
    public <T> Container<T> parametrized(
        @GraphQLTypeParam(
            types = {
                // XXX: overriding type here
                TargetSeven.class
            }
        )
            Class<T> type,
        DataFetchingEnvironment env
    )
    {
        return null;

    }
}
