package de.quinscape.domainql.logic;

import com.google.common.collect.BiMap;
import de.quinscape.domainql.DomainQLException;
import de.quinscape.domainql.param.ParameterProvider;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;

import java.util.Map;

/**
 * Provides parameter for GraphQL input type parameters.
 */
public class GraphQLValueProvider
    implements ParameterProvider<Object>
{
    private final static Logger log = LoggerFactory.getLogger(GraphQLValueProvider.class);

    private final String argumentName;

    private final String description;

    private final boolean isRequired;

    private final GraphQLInputType inputType;

    private final Object defaultValue;

    private final BiMap<Class<?>, String> inputTypes;

    private final Map<Class<? extends Enum>, GraphQLEnumType> registeredEnumTypes;


    public GraphQLValueProvider(
        String argumentName,
        String description,
        boolean isRequired,
        GraphQLInputType inputType,
        Object defaultValue,
        BiMap<Class<?>, String> inputTypes,
        Map<Class<? extends Enum>, GraphQLEnumType> registeredEnumTypes
    )
    {
        this.argumentName = argumentName;
        this.description = description;
        this.isRequired = isRequired;
        this.inputType = inputType;
        this.defaultValue = defaultValue;
        this.inputTypes = inputTypes;
        this.registeredEnumTypes = registeredEnumTypes;
    }


    public boolean isRequired()
    {
        return isRequired;
    }


    @Override
    public Object provide(DataFetchingEnvironment environment)
    {
        final GraphQLSchema schema = environment.getGraphQLSchema();

        Object value = environment.getArgument(argumentName);

        final GraphQLType type = schema.getType(inputType.getName());

        if (type instanceof GraphQLInputObjectType)
        {
            Class<?> pojoClass = inputTypes.inverse().get(inputType.getName());

            if (pojoClass == null)
            {
                throw new IllegalStateException("Cannot find pojo class for Input type '" + inputType + "'");
            }

            value = convert(pojoClass, (Map<String, Object>) value);
        }
        else if (type instanceof GraphQLEnumType && value instanceof String)
        {

            Class<? extends Enum> enumType = findPojoTypeForEnum((GraphQLEnumType) type);

            return Enum.valueOf(enumType, (String) value);
        }

        return value;
    }


    private Class<? extends Enum> findPojoTypeForEnum(GraphQLEnumType type)
    {
        for (Map.Entry<Class<? extends Enum>, GraphQLEnumType> e : registeredEnumTypes.entrySet())
        {
            if (e.getValue().equals(type))
            {
                return e.getKey();
            }
        }

        throw new IllegalStateException("No enum type registered for type: " + type);
    }


    private Object convert(Class<?> pojoClass, Map<String, Object> value)
    {

        try
        {
            if (pojoClass.isInstance(value))
            {
                return value;
            }

            final Object pojoInstance = pojoClass.newInstance();
            final JSONClassInfo classInfo = JSONUtil.getClassInfo(pojoClass);

            for (Map.Entry<String, Object> e : value.entrySet())
            {
                final String name = e.getKey();
                final JSONPropertyInfo propertyInfo = classInfo.getPropertyInfo(name);

                final Class<Object> propertyType = propertyInfo.getType();

                final Object orig = value.get(name);

                final Object converted;
                if (inputTypes.get(propertyType) != null)
                {
                    converted = convert(propertyType, (Map<String, Object>) orig);
                    JSONUtil.DEFAULT_UTIL.setProperty(pojoInstance, name, converted);
                }
                else
                {
                    converted = orig;
                }
                JSONUtil.DEFAULT_UTIL.setProperty(pojoInstance, name, converted);
            }
            return pojoInstance;

        }
        catch (Exception e)
        {
            throw new InputObjectConversionException(e);
        }
    }


    public String getArgumentName()
    {
        return argumentName;
    }


    public String getDescription()
    {
        return description;
    }


    public Object getDefaultValue()
    {
        return defaultValue;
    }


    public GraphQLInputType getInputType()
    {
        return inputType;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "argumentName = '" + argumentName + '\''
            + ", description = '" + description + '\''
            + ", inputType = " + inputType
            + ", defaultValue = " + defaultValue
            ;
    }
}
