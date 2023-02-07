package de.quinscape.domainql.generic;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.scalar.BigIntegerScalar;
import de.quinscape.domainql.schema.DomainQLAware;
import graphql.schema.GraphQLScalarType;

import java.math.BigInteger;
import java.util.Map;

public class GenericScalarType
{
    private GenericScalarType()
    {
        // no instances
    }

    private static final String NAME = "GenericScalar";

    public static GraphQLScalarType newGenericScalar()
    {
        return GraphQLScalarType.newScalar()
            .name(NAME)
            .description("Container for generic scalar values")
            .coercing(new GenericScalarCoercing())
            .build();
    }
}
