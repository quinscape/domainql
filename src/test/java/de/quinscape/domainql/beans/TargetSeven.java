package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLComputed;

public class TargetSeven
    extends de.quinscape.domainql.testdomain.tables.pojos.TargetSeven
{

    @GraphQLComputed
    public String getConcat()
    {
        return this.getId() + ":" + this.getName();
    }
}
