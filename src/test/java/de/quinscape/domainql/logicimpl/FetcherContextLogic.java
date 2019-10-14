package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.fetcher.FetcherContext;
import de.quinscape.domainql.testdomain.tables.pojos.SourceFive;
import de.quinscape.domainql.testdomain.tables.pojos.SourceSix;
import de.quinscape.domainql.testdomain.tables.pojos.TargetFive;
import de.quinscape.domainql.testdomain.tables.pojos.TargetSix;
import de.quinscape.domainql.testdomain.tables.pojos.TargetThree;
import de.quinscape.domainql.testdomain.tables.pojos.SourceThree;
import org.atteo.evo.inflector.English;

import java.util.Collections;

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

    @GraphQLQuery
    public TargetFive targetFiveWithFetcherContext()
    {
        final TargetFive targetFive = new TargetFive();

        targetFive.setId("target-five-0001");

        final SourceFive sourceFive = new SourceFive();
        sourceFive.setId("source-five-fetch-context");

        final FetcherContext fetcherContext = new FetcherContext();
        fetcherContext.setProperty("sourceFive", sourceFive);
        targetFive.provideFetcherContext(fetcherContext);

        return targetFive;
    }

    @GraphQLQuery
    public TargetSix targetSixWithFetcherContext()
    {
        final TargetSix targetSix = new TargetSix();

        targetSix.setId("target-six-0001");

        final SourceSix sourceSix = new SourceSix();
        sourceSix.setId("source-six-fetch-context");

        final FetcherContext fetcherContext = new FetcherContext();
        fetcherContext.setProperty("sourceSixes", Collections.singletonList(sourceSix));
        targetSix.provideFetcherContext(fetcherContext);

        return targetSix;
    }
}
