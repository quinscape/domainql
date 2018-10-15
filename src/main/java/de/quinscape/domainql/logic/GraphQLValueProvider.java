package de.quinscape.domainql.logic;

import de.quinscape.domainql.DomainQLException;
import de.quinscape.domainql.InputType;
import de.quinscape.domainql.OutputType;
import de.quinscape.domainql.TypeContext;
import de.quinscape.domainql.TypeRegistry;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.generic.GenericDomainObject;
import de.quinscape.domainql.param.ParameterProvider;
import de.quinscape.domainql.util.DegenerificationUtil;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.info.JavaObjectPropertyInfo;
import org.svenson.util.JSONBeanUtil;

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

    private final Class<?> parameterType;


    public GraphQLValueProvider(
        String argumentName,
        String description,
        boolean isRequired,
        GraphQLInputType inputType,
        Object defaultValue,
        TypeRegistry typeRegistry,
        Class<?> parameterType
    )
    {
        this.argumentName = argumentName;
        this.description = description;
        this.isRequired = isRequired;
        this.inputType = inputType;
        this.defaultValue = defaultValue;
        this.typeRegistry = typeRegistry;
        this.parameterType = parameterType;
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
            value = convert(environment, inputType, (Map<String, Object>) value);
        }
        else if (type instanceof GraphQLScalarType)
        {
            if (type.getName().equals("DomainObject"))
            {
                value = convertDomainObjectFields(environment.getGraphQLSchema(), parameterType, (DomainObject) value);
            }
        }

        return value;
    }


    private Object convert(
        DataFetchingEnvironment environment, InputType inputType, Map<String, Object> complexValue
    )
    {
        Class<?> pojoClass = inputType.getJavaType();
        try
        {
            if (pojoClass.isInstance(complexValue))
            {
                return complexValue;
            }

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

                    List<?> origList = (List) orig;
                    List<Object> convertedList = new ArrayList<>(origList.size());

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
                            if (DomainObject.class.isAssignableFrom(nextType))
                            {
                                convertedValue = convertDomainObjectFields(environment.getGraphQLSchema(), nextType, (DomainObject) elementValue);
                            }
                            else
                            {
                                convertedValue = elementValue;
                            }
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
                        converted = convert(environment, fieldType, (Map<String, Object>) orig);
                    }
                    else
                    {
                        if (DomainObject.class.isAssignableFrom(propertyType))
                        {
                            converted = convertDomainObjectFields(environment.getGraphQLSchema(), propertyType, (DomainObject) orig);
                        }
                        else
                        {
                            converted = orig;
                        }
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


    private Object convertDomainObjectFields(
        GraphQLSchema schema,
        Class<?> targetType,
        DomainObject domainObject
    )
    {
        if (!targetType.isInterface() && targetType.isInstance(domainObject))
        {
            return domainObject;
        }

        try
        {
            final JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;

            final String domainType = domainObject.getDomainType();

            final String inputTypeName = TypeRegistry.getInputTypeName(domainType);
            final GraphQLInputObjectType graphQLInputType = (GraphQLInputObjectType) schema.getType(inputTypeName);
            if (graphQLInputType == null)
            {
                throw new IllegalStateException("Could not find input type '" + inputTypeName + "'. You might need to add it as additional input type.");
            }

            final InputType inputType = typeRegistry.lookupInput(inputTypeName);

            Class<?> javaType = inputType.getJavaType();

            final Object convertedType = javaType.newInstance();

            final JSONClassInfo classInfo = JSONUtil.getClassInfo(javaType);
            for (GraphQLInputObjectField field : graphQLInputType.getFields())
            {
                final GraphQLInputType fieldType = field.getType();

                final GraphQLUnmodifiedType unwrapped = GraphQLTypeUtil.unwrapAll(fieldType);
                if (!(unwrapped instanceof GraphQLScalarType))
                {
                    throw new IllegalStateException(fieldType + " is not a scalar type");
                }

                final String name = field.getName();
                final Object value = domainObject.getProperty(name);
                final Object converted = ((GraphQLScalarType) unwrapped).getCoercing().parseValue(value);
                util.setProperty(convertedType, name, converted);
            }

            return convertedType;
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new DomainQLException(e);
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
