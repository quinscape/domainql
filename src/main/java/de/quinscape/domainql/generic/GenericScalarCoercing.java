package de.quinscape.domainql.generic;

import com.google.common.collect.Maps;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.schema.DomainQLAware;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GenericScalarCoercing
    implements Coercing<GenericScalar, Map<String, Object>>, DomainQLAware
{
    private final static Logger log = LoggerFactory.getLogger(GenericScalarCoercing.class);


    private DomainQL domainQL;


    public GenericScalarCoercing()
    {
    }


    @Override
    public Map<String, Object> serialize(Object result) throws CoercingSerializeException
    {
        try
        {
            if (!(result instanceof GenericScalar))
            {
                throw new IllegalArgumentException(result + " is not an instance of " + GenericScalar.class.getName());
            }

            final GenericScalar genericScalar = (GenericScalar) result;
            final String scalarTypeName = genericScalar.getType();
            final GraphQLType type = getScalarType(scalarTypeName);


            final Object converted;
            final Object value = ((GenericScalar) result).getValue();
            if (type instanceof GraphQLList)
            {

                List<Object> l = (List<Object>) value;
                if (l == null)
                {
                    converted = null;

                }
                else
                {
                    List<Object> out = new ArrayList<>(l.size());

                    final Coercing<?,?> coercing = ((GraphQLScalarType)(GraphQLTypeUtil.unwrapAll(type))).getCoercing();

                    for (Object elem : l)
                    {
                        final Object convertedElem = coercing
                            .serialize(elem);
                        out.add(convertedElem);
                    }

                    converted = out;
                }
            }
            else
            {
                converted = value != null ? ((GraphQLScalarType) type).getCoercing().serialize(value) : null;

            }
            Map<String, Object> map = Maps.newHashMapWithExpectedSize(2);
            map.put("type", scalarTypeName);
            map.put("value", converted);

            return map;

        }
        catch (RuntimeException e)
        {
            if (!(e instanceof CoercingSerializeException))
            {
                // ensure stacktrace logging for GraphQL validation errors
                log.error("Error serializing generic scalar", e);
            }
            throw new CoercingSerializeException(e);
        }
    }


    private GraphQLType getScalarType(String genericScalarType)
    {
        if (genericScalarType.startsWith("[") && genericScalarType.endsWith("]"))
        {
            final GraphQLType type = getScalarInternal(genericScalarType.substring(1, genericScalarType.length() - 1));
            return GraphQLList.list(type);
        }
        return getScalarInternal(genericScalarType);
    }

    private GraphQLType getScalarInternal(String genericScalarType)
    {
        final GraphQLType type = domainQL.getGraphQLSchema().getType(genericScalarType);

        if (!(type instanceof GraphQLScalarType))
        {
            throw new IllegalStateException("Type '" + genericScalarType + "' is not a scalar type: " + type);
        }
        return type;
    }


    @Override
    public GenericScalar parseValue(Object input) throws CoercingParseValueException
    {
        try
        {
            if (!(input instanceof Map))
            {
                throw new CoercingParseValueException("Cannot coerce " + input +
                    " to GenericScalar, must be map with 'type' and 'value' property");
            }

            final Map<String,Object> genericScalarMap = (Map) input;
            final String scalarTypeName = (String) genericScalarMap.get("type");
            final Object value = genericScalarMap.get("value");

            final GraphQLType type = getScalarType(scalarTypeName);

            if (type instanceof GraphQLList)
            {
                List<Object> l = (List<Object>) value;
                if (l == null)
                {
                    return new GenericScalar(scalarTypeName, null);
                }
                else
                {
                    List<Object> out = new ArrayList<>(l.size());

                    final Coercing<?, ?> coercing = ((GraphQLScalarType) (GraphQLTypeUtil.unwrapAll(type))).getCoercing();

                    for (Object elem : l)
                    {
                        final Object converted = coercing
                            .parseValue(elem);
                        out.add(converted);
                    }
                    return new GenericScalar(scalarTypeName, out);
                }

            }
            else
            {

                final Coercing<?,?> coercing = ((GraphQLScalarType) type).getCoercing();
                final Object converted = value != null ? coercing.parseValue(value) : null;

                return new GenericScalar(scalarTypeName, converted);
            }

        }
        catch (RuntimeException e)
        {
            if (!(e instanceof CoercingParseValueException))
            {
                // ensure stacktrace logging for GraphQL validation errors
                log.error("Error parsing generic scalar", e);
            }
            throw new CoercingParseValueException(e);
        }
    }


    @Override
    public GenericScalar parseLiteral(Object input) throws CoercingParseLiteralException
    {
        // XXX: this should be possible
        throw new CoercingParseLiteralException("Cannot coerce GenericScalarType from literal");
    }


    @Override
    public void setDomainQL(DomainQL domainQL)
    {
        this.domainQL = domainQL;
    }
}
