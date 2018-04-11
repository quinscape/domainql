package de.quinscape.domainql.config;

/**
 * DomainQL behaviour for the source / left-hand side of a foreign Key.
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
    OBJECT,

    /**
     *  Define a field for the key iteself *and* define an embedded object.
     *  <p>
     *      This is useful in situtations where you want the embedded object in some cases, but in others you
     *      want to save one level of querying because all you need is the target id.
     *  </p>
     */
    OBJECT_AND_SCALAR
}
