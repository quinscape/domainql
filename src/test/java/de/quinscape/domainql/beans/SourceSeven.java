package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLComputed;

public class SourceSeven
    extends de.quinscape.domainql.testdomain.tables.pojos.SourceSeven
{

    @GraphQLComputed
    public String getConcat()
    {
        return this.getId() + ":" + this.getTarget();
    }
}
