package de.quinscape.domainql.schema;

import de.quinscape.domainql.DomainQL;
import graphql.schema.GraphQLSchema;

public interface DomainQLAware
{
    void registerSchema(DomainQL domainQL, GraphQLSchema schema);
}
