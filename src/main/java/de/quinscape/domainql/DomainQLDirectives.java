package de.quinscape.domainql;

import graphql.introspection.Introspection;
import graphql.schema.GraphQLDirective;

/**
 * Container for DomainQL specific schema directives
 */
public class DomainQLDirectives
{
    private DomainQLDirectives()
    {
        // no instances
    }


    /**
     * A computed directive that is the result of using @GraphQLComputed, e.g. when overriding a database output type.
     */
    public final static GraphQLDirective ComputedDirective = GraphQLDirective.newDirective()
        .name("computed")
        .validLocation(Introspection.DirectiveLocation.FIELD_DEFINITION)
        .build();
}
