package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.fetcher.FetcherContext;
import de.quinscape.domainql.testdomain.tables.pojos.TargetThree;
import de.quinscape.domainql.testdomain.tables.pojos.SourceThree;

@GraphQLLogic
public class FetcherContextLogic
{
    @GraphQLQuery
    public SourceThree sourceThreeWithFetcherContext()
    {
        final SourceThree sourceThree = new SourceThree();

        sourceThree.setId("source-three-0001");

        final TargetThree targetThree = new TargetThree();
        targetThree.setId("target-three-fetch-context");

        final FetcherContext fetcherContext = new FetcherContext();
        fetcherContext.setProperty("target", targetThree);
        sourceThree.provideFetcherContext(fetcherContext);

        return sourceThree;
    }
}
