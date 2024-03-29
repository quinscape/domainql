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
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
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

    private final boolean isNotNull;

    private final GraphQLInputType inputType;

    private final Object defaultValue;

    private final TypeRegistry typeRegistry;

    private final Class<?> parameterType;


    public GraphQLValueProvider(
        String argumentName,
        String description,
        boolean isNotNull,
        GraphQLInputType inputType,
        Object defaultValue,
        TypeRegistry typeRegistry,
        Class<?> parameterType
    )
    {
        this.argumentName = argumentName;
        this.description = description;
        this.isNotNull = isNotNull;
        this.inputType = inputType;
        this.defaultValue = defaultValue;
        this.typeRegistry = typeRegistry;
        this.parameterType = parameterType;
    }

    public boolean isNotNull()
    {
        return isNotNull;
    }


    @Override
    public Object provide(DataFetchingEnvironment environment)
    {
        final GraphQLSchema schema = environment.getGraphQLSchema();

        Object value = environment.getArgument(argumentName);

        if (inputType instanceof GraphQLList)
        {
            if (value != null)
            {
                final List<Object> valueList = (List<Object>) value;
                final List<Object> converted  = new ArrayList<>(valueList.size());

                for (Object o : valueList)
                {
                    converted.add(
                        convertObj(environment, o, ((GraphQLList) inputType).getWrappedType())
                    );
                }

                value = converted;
            }
        }
        else
        {
            // inputType is a GraphQLTypeReference, so we have to get the actual type
            final GraphQLType type = schema.getType(((GraphQLNamedType)inputType).getName());
            value = convertObj(environment, value, type);
        }
        return value;
    }


    private Object convertObj(DataFetchingEnvironment environment, Object value, GraphQLType type)
    {
        if (type instanceof GraphQLEnumType && value instanceof String)
        {
            final InputType inputType = typeRegistry.lookupInput(((GraphQLNamedType)type).getName());

            final Class<?> javaType = inputType.getJavaType();
            if (!Enum.class.isAssignableFrom(javaType))
            {
                throw new IllegalStateException(inputType + " is not an enum type");
            }

            Class<? extends Enum> enumType = (Class<? extends Enum>) javaType;

            value = Enum.valueOf(enumType, (String) value);
        }
        else if (type instanceof GraphQLInputObjectType)
        {
            final InputType inputType = typeRegistry.lookupInput(((GraphQLNamedType)type).getName());

            if (inputType == null)
            {
                throw new IllegalStateException("Could not find input type '" + ((GraphQLNamedType)this.inputType).getName() + "'");
            }
            value = convert(environment, inputType, (Map<String, Object>) value);
        }
        return value;
    }


    private Object convert(
        DataFetchingEnvironment environment, InputType inputType, Map<String, Object> complexValue
    )
    {
        if (complexValue == null)
        {
            return null;
        }

        Class<?> pojoClass = inputType.getJavaType();
        if (pojoClass.isInstance(complexValue))
        {
            return complexValue;
        }
        try
        {

            final Object pojoInstance = pojoClass.newInstance();
            final JSONClassInfo classInfo = JSONUtil.getClassInfo(pojoClass);

            for (Map.Entry<String, Object> e : complexValue.entrySet())
            {
                final String name = e.getKey();
                final JSONPropertyInfo propertyInfo = classInfo.getPropertyInfo(name);

                final Method getterMethod = ((JavaObjectPropertyInfo) propertyInfo).getGetterMethod();

                final Class<Object> propertyType = propertyInfo.getType();
                final Object orig = complexValue.get(name);

                final Object converted;

                if (List.class.isAssignableFrom(propertyType))
                {
                    Class<?> nextType = DegenerificationUtil.getElementType(inputType, getterMethod);

                    List<Object> convertedList = null;
                    if (orig != null)
                    {
                        List<?> origList = (List) orig;
                        convertedList = new ArrayList<>(origList.size());

                        final InputType fieldType = typeRegistry.lookupInput(
                            new TypeContext(inputType.getTypeContext(), nextType));

                        for (Object elementValue : origList)
                        {
                            Object convertedValue;
                            if (fieldType != null && !fieldType.isEnum())
                            {
                                convertedValue = convert(environment, fieldType, (Map<String, Object>) elementValue);
                            }
                            else
                            {
                                convertedValue = elementValue;
                            }
                            convertedList.add(convertedValue);

                        }
                    }
                    JSONUtil.DEFAULT_UTIL.setProperty(pojoInstance, name, convertedList);
                }
                else
                {
                    final TypeContext typeContext = DegenerificationUtil.getType(inputType.getTypeContext(), inputType, getterMethod);
                    final InputType fieldType = typeRegistry.lookupInput(typeContext);

                    if (fieldType != null && !fieldType.isEnum())
                    {
                        converted = convert(environment, fieldType, (Map<String, Object>) orig);
                    }
                    else
                    {
                        converted = orig;
                    }

                    if (converted == null && propertyInfo.getType().isPrimitive())
                    {
                        throw new InputObjectConversionException(
                            "Cannot set primitive property " + pojoClass.getName() + "#" + name + " to null"
                        );
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
