package de.quinscape.domainql.config;

/**
 * DomainQL behaviour for the source side of a foreign Key.
 *
 * What kind of field is generated on the side that contains the foreign key.
 */
public enum SourceField
{
    /**
     * Ignore field for source type.
     */
    NONE,
    /**
     * Define a scalar GraphQL field for the key itself (e.g. fooId : string)
     */
    SCALAR,
    /**
     * Define an embedded object for the target
     */
    OBJECT
}
