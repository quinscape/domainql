package de.quinscape.domainql.logic;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.BeanWithFetcher;

@GraphQLLogic
public class CustomFetcherLogic
{
    @GraphQLQuery
    public BeanWithFetcher beanWithFetcher()
    {
        final BeanWithFetcher bean = new BeanWithFetcher();
        bean.setValue("Value From Logic");
        return bean;
    }
}
