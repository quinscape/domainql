package de.quinscape.domainql.generic;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.schema.DomainQLAware;

import java.util.Map;

public class GenericScalarType
    extends graphql.schema.GraphQLScalarType
    implements DomainQLAware

{
    private static final String NAME = "GenericScalar";

    private final DelayedCoercing<GenericScalar, Map<String, Object>> coercing;


    private GenericScalarType(DelayedCoercing<GenericScalar, Map<String, Object>> coercing)
    {

        super(
            NAME, "Container for generic scalar values", coercing
        );

        this.coercing = coercing;
    }


    public static GenericScalarType newGenericScalar()
    {
        return new GenericScalarType(new DelayedCoercing<>());
    }

    public void setDomainQL(DomainQL domainQL)
    {
        this.coercing.setTarget(new GenericScalarCoercing(domainQL));
    }
}
