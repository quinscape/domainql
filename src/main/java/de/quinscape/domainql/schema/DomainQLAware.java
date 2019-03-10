package de.quinscape.domainql.schema;

import de.quinscape.domainql.DomainQL;

/**
 * Implemented by GraphQL scalar implementations that need to know about the GraphQL schema and the meta data contained
 * in the current DomainQL instance.
 */
public interface DomainQLAware
{
    /**
     * Provides the current DomainQL instance.
     *
     * @param domainQL DomainQL instance
     */
    void setDomainQL(DomainQL domainQL);
}
