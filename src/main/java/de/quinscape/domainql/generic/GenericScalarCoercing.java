package de.quinscape.domainql.generic;

import com.google.common.collect.Maps;
import de.quinscape.domainql.DomainQL;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;

import java.util.Map;

public final class GenericScalarCoercing
    implements Coercing<GenericScalar, Map<String, Object>>
{
    private final DomainQL domainQL;


    public GenericScalarCoercing(DomainQL domainQL)
    {
        this.domainQL = domainQL;
    }


    @Override
    public Map<String, Object> serialize(Object result) throws CoercingSerializeException
    {
        if (!(result instanceof GenericScalar))
        {
            throw new IllegalArgumentException(result + " is not an instance of " + GenericScalar.class.getName());
        }

        final GenericScalar genericScalar = (GenericScalar) result;
        final String scalarTypeName = genericScalar.getType();
        final GraphQLType type = getScalarType(scalarTypeName);

        final Object converted = ((GraphQLScalarType) type).getCoercing()
            .serialize(((GenericScalar) result).getValue());

        Map<String, Object> map = Maps.newHashMapWithExpectedSize(2);
        map.put("type", scalarTypeName);
        map.put("value", converted);

        return map;
    }


    private GraphQLType getScalarType(String scaleTypeName)
    {
        final GraphQLType type = domainQL.getGraphQLSchema().getType(scaleTypeName);

        if (!(type instanceof GraphQLScalarType))
        {
            throw new IllegalStateException("Type '" + scaleTypeName + "' is not a scalar type: " + type);
        }
        return type;
    }


    @Override
    public GenericScalar parseValue(Object input) throws CoercingParseValueException
    {
        if (!(input instanceof Map))
        {
            throw new CoercingParseValueException("Cannot coerce " + input + " to DomainObject");
        }

        final Map<String,Object> genericScalarMap = (Map) input;
        final String scalarTypeName = (String) genericScalarMap.get("type");
        final Object value = genericScalarMap.get("value");

        final GraphQLType type = getScalarType(scalarTypeName);

        final Object converted = ((GraphQLScalarType) type).getCoercing()
            .parseValue(value);

        return new GenericScalar(scalarTypeName, converted);
    }


    @Override
    public GenericScalar parseLiteral(Object input) throws CoercingParseLiteralException
    {
        // XXX: this should be possible
        throw new CoercingParseLiteralException("Cannot coerce GenericScalarType from literal");
    }
}
