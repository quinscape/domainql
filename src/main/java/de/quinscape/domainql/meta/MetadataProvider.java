package de.quinscape.domainql.meta;

import de.quinscape.domainql.DomainQL;

/**
 * Implemented by classes that want to contribute schema metadata to the DomainQL meta data.
 *
 * @see de.quinscape.domainql.DomainQLBuilder#withMetadataProviders(MetadataProvider...) 
 */
public interface MetadataProvider
{
    void provideMetaData(DomainQL domainQL, DomainQLMeta meta);
}
