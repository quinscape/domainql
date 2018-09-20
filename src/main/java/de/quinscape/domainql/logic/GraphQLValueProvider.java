package de.quinscape.domainql.logic;

import de.quinscape.domainql.InputType;
import de.quinscape.domainql.OutputType;
import de.quinscape.domainql.TypeContext;
import de.quinscape.domainql.TypeRegistry;
import de.quinscape.domainql.param.ParameterProvider;
import de.quinscape.domainql.util.DegenerificationUtil;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.info.JavaObjectPropertyInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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

    private final TypeRegistry typeRegistry;


    public GraphQLValueProvider(
        String argumentName,
        String description,
        boolean isRequired,
        GraphQLInputType inputType,
        Object defaultValue,
        TypeRegistry typeRegistry
    )
    {
        this.argumentName = argumentName;
        this.description = description;
        this.isRequired = isRequired;
        this.inputType = inputType;
        this.defaultValue = defaultValue;
        this.typeRegistry = typeRegistry;
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

        if (type instanceof GraphQLEnumType && value instanceof String)
        {

            final OutputType outputType = typeRegistry.lookup(type.getName());

            final Class<?> javaType = outputType.getJavaType();
            if (!Enum.class.isAssignableFrom(javaType))
            {
                throw new IllegalStateException(outputType + " is not an enum type");
            }

            Class<? extends Enum> enumType = (Class<? extends Enum>) javaType;

            return Enum.valueOf(enumType, (String) value);
        }
        else if (type instanceof GraphQLInputObjectType)
        {
            final InputType inputType = typeRegistry.lookupInput(this.inputType.getName());

            if (inputType == null)
            {
                throw new IllegalStateException("Could not find input type '" + this.inputType.getName() + "'");
            }
            value = convert(inputType, (Map<String, Object>) value);
        }

        return value;
    }


    private Object convert(InputType inputType, Map<String, Object> value)
    {
        Class<?> pojoClass = inputType.getJavaType();
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

                final Method getterMethod = ((JavaObjectPropertyInfo) propertyInfo).getGetterMethod();

                final Class<Object> propertyType = propertyInfo.getType();
                final Object orig = value.get(name);

                final Object converted;

                if (List.class.isAssignableFrom(propertyType))
                {
                    Class<?> nextType = DegenerificationUtil.getElementType(inputType, getterMethod);

                    List<?> origList = (List) orig;
                    List<Object> convertedList = new ArrayList<>(origList.size());

                    final InputType fieldType = typeRegistry.lookupInput(
                        new TypeContext(inputType.getTypeContext(), nextType));

                    for (Object origValue : origList)
                    {
                        Object convertedValue;
                        if (fieldType != null && !fieldType.isEnum())
                        {
                            convertedValue = convert(fieldType, (Map<String, Object>) origValue);
                        }
                        else
                        {
                            convertedValue = orig;
                        }
                        convertedList.add(convertedValue);

                    }
                    JSONUtil.DEFAULT_UTIL.setProperty(pojoInstance, name, convertedList);
                }
                else
                {
                    final TypeContext typeContext = DegenerificationUtil.getType(inputType.getTypeContext(), inputType, getterMethod);
                    final InputType fieldType = typeRegistry.lookupInput(typeContext);

                    if (fieldType != null && !fieldType.isEnum())
                    {
                        converted = convert(fieldType, (Map<String, Object>) orig);
                    }
                    else
                    {
                        converted = orig;
                    }
                    JSONUtil.DEFAULT_UTIL.setProperty(pojoInstance, name, converted);
                }
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
