package de.quinscape.domainql.generic;

import com.google.common.collect.Maps;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.DomainQLException;
import de.quinscape.domainql.InputType;
import de.quinscape.domainql.schema.DomainQLAware;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public final class DomainObjectCoercing
    implements Coercing<DomainObject, Map<String, Object>>, DomainQLAware
{
    private final static Logger log = LoggerFactory.getLogger(DomainObjectCoercing.class);


    private DomainQL domainQL;


    public DomainObjectCoercing()
    {
    }


    @Override
    public Map<String, Object> serialize(Object result) throws CoercingSerializeException
    {
        try
        {
            return serializeInternal(result);
        }
        catch(RuntimeException e)
        {
            if (!(e instanceof CoercingSerializeException))
            {
                // ensure stacktrace logging for GraphQL validation errors
                log.error("Error serializing domain object", e);
            }

            throw new CoercingSerializeException(e);
        }
    }


    @Override
    public DomainObject parseValue(Object input) throws CoercingParseValueException
    {
        try
        {
            return parseValueInternal(input);
        }
        catch(RuntimeException e)
        {
            if (!(e instanceof CoercingParseValueException))
            {
                // ensure stacktrace logging for GraphQL validation errors
                log.error("Error serializing domain object", e);
            }

            throw new CoercingParseValueException(e);
        }
    }


    private Map<String, Object> serializeInternal(Object result)
    {
        if (result == null)
        {
            return null;
        }

        if (!(result instanceof DomainObject))
        {
            throw new IllegalArgumentException(result + " is not an instance of " + DomainObject.class.getName());
        }

        final DomainObject domainObject = (DomainObject) result;

        final String domainType = domainObject.getDomainType();

        final GraphQLSchema schema = domainQL.getGraphQLSchema();
        final GraphQLType type = schema.getType(domainType);
        if (!(type instanceof GraphQLObjectType))
        {
            throw new IllegalStateException("Expected '" + domainType + "' to be an object type, but it is: " + type);
        }

        final GraphQLObjectType objectType = (GraphQLObjectType) type;


        final List<GraphQLFieldDefinition> fieldDefinitions = objectType.getFieldDefinitions();

        final Map<String, Object> convertedType;
        if (domainObject instanceof GenericDomainObject)
        {
            convertedType = ((GenericDomainObject) domainObject).contents();
        }
        else
        {
            convertedType = Maps.newHashMapWithExpectedSize(fieldDefinitions.size());

        }


        for (GraphQLFieldDefinition fieldDefinition : fieldDefinitions)
        {
            final String fieldName = fieldDefinition.getName();

            final GraphQLUnmodifiedType unwrapped = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

            // we only want to convert scalar types
            if (unwrapped instanceof GraphQLScalarType)
            {
                final Object value = domainObject.getProperty(fieldName);

                GraphQLScalarType scalarType = (GraphQLScalarType) unwrapped;

                final Object converted;
                if (value != null)
                {
                    converted = scalarType.getCoercing().serialize(value);
                }
                else
                {
                    if (GraphQLTypeUtil.isNonNull(fieldDefinition.getType()))
                    {
                        throw new IllegalStateException("Non-null field '" + fieldDefinition.getName() + "' contains null value");
                    }

                    converted = null;
                }
                convertedType.put(fieldName, converted);
            }
        }

        convertedType.put(DomainObject.DOMAIN_TYPE_PROPERTY, domainType);

        return convertedType;
    }


    private DomainObject parseValueInternal(Object input)
    {
        if (input == null)
        {
            return null;
        }

        if (!(input instanceof Map))
        {
            throw new CoercingParseValueException("Cannot coerce " + input + " to DomainObject");
        }


        final Map<String, Object> map = (Map<String, Object>) input;

        final String domainType = (String) map.get(DomainObject.DOMAIN_TYPE_PROPERTY);
        final String inputTypeName = DomainQL.getInputTypeName(domainType);

        final GraphQLSchema schema = domainQL.getGraphQLSchema();
        final GraphQLType gqlType = schema.getType(inputTypeName);
        if (!(gqlType instanceof GraphQLInputObjectType))
        {
            throw new IllegalStateException("Expected '" + inputTypeName + "' to be an object type, but it is: " + gqlType);
        }

        final InputType inputType = domainQL.getTypeRegistry().lookupInput(inputTypeName);
        if (inputType == null)
        {
            throw new IllegalStateException("Invalid input type '" + inputTypeName + "'");
        }

        try
        {
            final DomainObject convertedType = (DomainObject) inputType.getJavaType().newInstance();

            final List<GraphQLInputObjectField> fieldDefinitions = ((GraphQLInputObjectType) gqlType).getFieldDefinitions();

            for (GraphQLInputObjectField fieldDefinition : fieldDefinitions)
            {
                final String fieldName = fieldDefinition.getName();
                final Object value = map.get(fieldName);

                final Object converted;
                if (value != null)
                {
                    // domain object inputs have only scalar types
                    GraphQLScalarType scalarType = (GraphQLScalarType) GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

                    converted = scalarType.getCoercing().parseValue(value);
                }
                else
                {
                    converted = null;
                }

                convertedType.setProperty(fieldName, converted);
            }
            return convertedType;

        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new DomainQLException(e);
        }
    }


    @Override
    public DomainObject parseLiteral(Object input) throws CoercingParseLiteralException
    {
        throw new CoercingParseLiteralException("Cannot coerce DomainObject from literal");
    }


    @Override
    public void setDomainQL(DomainQL domainQL)
    {
        this.domainQL = domainQL;
    }
}
