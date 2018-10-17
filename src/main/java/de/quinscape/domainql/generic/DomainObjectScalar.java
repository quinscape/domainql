package de.quinscape.domainql.generic;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.schema.DomainQLAware;
import graphql.schema.GraphQLSchema;

import java.util.Map;

/**
 * GraphQL Scalar implementation that wraps another domain type. The value is transmitted as a scalar but does the
 * appropriate conversions for the wrapped domain object.
 * <p>
 * This scalar allows to write GraphQL mutations that accept any of schema domain types. It requires that all domain
 * object
 * beans implement {@link DomainObject}. Configure the {@link DomainObjectGeneratorStrategy} for jooq to automatically
 * make the generated POJOs implement that interface.
 *
 * @see DomainObject
 */
public class DomainObjectScalar
    extends graphql.schema.GraphQLScalarType
    implements DomainQLAware

{
    private final DelayedCoercing<DomainObject, Map<String, Object>> coercing;


    private DomainObjectScalar(DelayedCoercing<DomainObject, Map<String, Object>> coercing)
    {

        super(
            "DomainObject", "Container for generic domain objects as scalar", coercing
        );

        this.coercing = coercing;
    }


    public static DomainObjectScalar newDomainObjectScalar()
    {
        return new DomainObjectScalar(new DelayedCoercing<>());
    }

    public void registerSchema(DomainQL domainQL, GraphQLSchema schema)
    {
        this.coercing.setTarget(new DomainObjectCoercing(domainQL, schema));
    }
}
