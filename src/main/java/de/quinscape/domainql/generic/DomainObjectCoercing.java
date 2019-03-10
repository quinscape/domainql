package de.quinscape.domainql.generic;

import com.google.common.collect.Maps;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.DomainQLException;
import de.quinscape.domainql.InputType;
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

import java.util.List;
import java.util.Map;

public class DomainObjectCoercing
    implements Coercing<DomainObject, Map<String, Object>>
{
    private final DomainQL domainQL;


    public DomainObjectCoercing(DomainQL domainQL)
    {
        this.domainQL = domainQL;
    }


    @Override
    public Map<String, Object> serialize(Object result) throws CoercingSerializeException
    {
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
            final Object value = domainObject.getProperty(fieldName);

            // domain objects have only scalar types
            GraphQLScalarType scalarType = (GraphQLScalarType) GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

            final Object converted = scalarType.getCoercing().serialize(value);

            convertedType.put(fieldName, converted);
        }
        return convertedType;
    }


    @Override
    public DomainObject parseValue(Object input) throws CoercingParseValueException
    {
        if (!(input instanceof Map))
        {
            throw new CoercingParseValueException("Cannot coerce " + input + " to DomainObject");
        }


        final Map<String, Object> map = (Map<String, Object>) input;

        final String domainType = (String) map.get("_type");
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
}
