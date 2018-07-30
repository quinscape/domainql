package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;

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
