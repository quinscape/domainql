package de.quinscape.domainql;

import de.quinscape.domainql.annotation.GraphQLField;
import de.quinscape.domainql.scalar.GraphQLCurrencyScalar;
import de.quinscape.domainql.scalar.GraphQLDateScalar;
import de.quinscape.domainql.scalar.GraphQLTimestampScalar;
import de.quinscape.domainql.util.DegenerificationUtil;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.Scalars;
import graphql.schema.GraphQLScalarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.info.JavaObjectPropertyInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeRegistry
{
    private final static Logger log = LoggerFactory.getLogger(TypeRegistry.class);

    /**
     * Default scalar types
     */
    private final static Map<Class<?>, GraphQLScalarType> JAVA_TYPE_TO_GRAPHQL;
    static
    {
        final Map<Class<?>, GraphQLScalarType> map = new HashMap<>();

        map.put(Boolean.class, Scalars.GraphQLBoolean);
        map.put(Boolean.TYPE, Scalars.GraphQLBoolean);
        map.put(Byte.class, Scalars.GraphQLByte);
        map.put(Byte.TYPE, Scalars.GraphQLByte);
        map.put(Short.class, Scalars.GraphQLShort);
        map.put(Short.TYPE, Scalars.GraphQLShort);
        map.put(Integer.class, Scalars.GraphQLInt);
        map.put(Integer.TYPE, Scalars.GraphQLInt);
        map.put(Double.class, Scalars.GraphQLFloat);
        map.put(Double.TYPE, Scalars.GraphQLFloat);
        map.put(Long.class, Scalars.GraphQLLong);
        map.put(Long.TYPE, Scalars.GraphQLLong);
        map.put(String.class, Scalars.GraphQLString);
        map.put(BigDecimal.class, Scalars.GraphQLBigDecimal);
        map.put(BigInteger.class, Scalars.GraphQLBigInteger);
        map.put(Timestamp.class, new GraphQLTimestampScalar());
        map.put(Date.class, new GraphQLDateScalar());

        JAVA_TYPE_TO_GRAPHQL = Collections.unmodifiableMap(map);
    }

    private final DomainQL domainQL;

    private final Map<String, GraphQLScalarType> scalarTypeByName;

    private final Map<Class<?>, GraphQLScalarType> scalarTypeByClass;


    private Map<TypeContext, InputType> inputTypes = new HashMap<>();

    private Map<TypeContext, OutputType> outputTypes = new HashMap<>();


    public TypeRegistry(
        DomainQL domainQL, Map<Class<?>,
        GraphQLScalarType> additionalScalarTypes
    )
    {
        this.domainQL = domainQL;
        final Map<Class<?>, GraphQLScalarType> scalarTypeByClass = new HashMap<>(JAVA_TYPE_TO_GRAPHQL);
        scalarTypeByClass.putAll(additionalScalarTypes);

        this.scalarTypeByName = Collections.unmodifiableMap(mapByName(scalarTypeByClass));
        this.scalarTypeByClass = Collections.unmodifiableMap(scalarTypeByClass);
    }

    private Map<String, GraphQLScalarType> mapByName(Map<Class<?>, GraphQLScalarType> scalarTypeByClass)
    {
        final Map<String, GraphQLScalarType> map = new HashMap<>();

        for (GraphQLScalarType scalarType : scalarTypeByClass.values())
        {
            map.put(scalarType.getName(), scalarType);
        }

        return map;
    }



    public InputType registerInput(TypeContext typeContext)
    {

        return registerInput(typeContext, null);
    }


    public InputType registerInput(TypeContext typeContext, TypeContext parentContext)
    {

        final Class<?> javaType = typeContext.getType();
        DomainQL.ensurePojoType(javaType);


        final InputType existing = inputTypes.get(typeContext);
        if (existing != null)
        {
            return existing;
        }

        if (Enum.class.isAssignableFrom(javaType))
        {
            final InputType enumType = new InputType(javaType.getSimpleName(), typeContext);
            inputTypes.put(typeContext, enumType);
            return enumType;
        }

        final String inputTypeName = getInputTypeName(typeContext.getTypeName());

        final InputType newType = new InputType(inputTypeName, typeContext);

        inputTypes.put(typeContext, newType);

        final Type[] actualTypeArguments = typeContext.getActualTypeArguments();

        if (actualTypeArguments != null)
        {
            for (Type type : actualTypeArguments)
            {
                if (type instanceof TypeVariable)
                {
                    final Class<?> resolved = parentContext.resolveType(((TypeVariable) type).getName());
                    registerInput(new TypeContext(typeContext, resolved));

                }
                else
                {
                    registerInput(new TypeContext(typeContext, (Class<?>) type));
                }
            }
        }

        registerFields(
            newType,
            typeContext
        );

        return newType;
    }

    public static String getInputTypeName(String outputClassName)
    {
        if (outputClassName == null)
        {
            throw new IllegalArgumentException("outputClassName can't be null");
        }


        if (outputClassName.endsWith(DomainQL.INPUT_SUFFIX))
        {
            return outputClassName;
        }
        else
        {
            return outputClassName + DomainQL.INPUT_SUFFIX;
        }
    }

    public OutputType register(TypeContext ctx)
    {
        return register(ctx, null);
    }




    public OutputType register(TypeContext ctx, TypeContext parentContext)
    {
        final Class<?> javaType = ctx.getType();

        if (javaType.getName().equals("de.quinscape.domainql.beans.AnnotatedBean"))
        {
            log.info("hit");
        }

        DomainQL.ensurePojoType(javaType);

        final OutputType existing = outputTypes.get(ctx);
        if (existing != null)
        {
            return existing;
        }

        if (Enum.class.isAssignableFrom(javaType))
        {
            final OutputType enumType = new OutputType(ctx, javaType);
            outputTypes.put(ctx, enumType);
            return enumType;
        }


        final OutputType newType = new OutputType(ctx, javaType);

        outputTypes.put(ctx, newType);

        final Type[] actualTypeArguments = ctx.getActualTypeArguments();

        if (actualTypeArguments != null)
        {
            for (Type type : actualTypeArguments)
            {
                final Class<?> typeArg;
                if (type instanceof TypeVariable)
                {
                    Class<?> resolved;
                    if (parentContext == null || (resolved = parentContext.resolveType(
                        ((TypeVariable) type).getName())) == null)
                    {
                        throw new IllegalStateException(
                            "Error registering " + javaType.getName() + ": Type ist not a concrete class: " + Arrays
                                .toString(actualTypeArguments));
                    }
                    typeArg = resolved;
                }
                else
                {
                    typeArg = (Class<?>) type;
                }

                register(new TypeContext(ctx, typeArg), ctx);
            }
        }

        registerFields(
            newType,
            ctx
        );

        return newType;
    }


    public InputType lookupInput(TypeContext typeContext)
    {
        return inputTypes.get(typeContext);
    }

    public InputType lookupInput(String name)
    {

        for (InputType inputType : inputTypes.values())
        {
            if (inputType.getName().equals(name))
            {
                return inputType;
            }
        }
        return null;
    }


    public OutputType lookup(String name)
    {
        for (OutputType outputType : outputTypes.values())
        {
            if (outputType.getName().equals(name))
            {
                return outputType;
            }

        }
        return null;
    }


    public OutputType lookup(Class<?> cls)
    {
        for (OutputType outputType : outputTypes.values())
        {
            if (outputType.getJavaType().equals(cls))
            {
                return outputType;
            }

        }
        return null;
    }


    public Collection<OutputType> getOutputTypes()
    {
        return outputTypes.values();
    }


    private void registerFields(
        ComplexType complexType,
        TypeContext parentContext
    )
    {
        final Class<?> javaType = complexType.getJavaType();

        final JSONClassInfo classInfo = JSONUtil.getClassInfo(javaType);
        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {
            final Class<Object> type = info.getType();

            final GraphQLField graphQLFieldAnno = JSONUtil.findAnnotation(info, GraphQLField.class);


            if (!DomainQL.isNormalProperty(info))
            {
                continue;
            }

            //registerNewOutputType.accept(complexType);

            final Method getterMethod = ((JavaObjectPropertyInfo) info).getGetterMethod();

            TypeContext ctx = new TypeContext(parentContext, getterMethod);

            final Class<?> nextType;
            if (List.class.isAssignableFrom(type))
            {
                nextType = DegenerificationUtil.getElementType(complexType, getterMethod);
                // create new type context for the element of the generic list
                ctx = new TypeContext(ctx, nextType);
            }
            else
            {
                ctx = DegenerificationUtil.getType(parentContext, complexType, getterMethod);
                nextType = ctx.getType();
            }

            if (getGraphQLScalarFor(nextType, graphQLFieldAnno) == null)
            {
                boolean isInput = complexType instanceof InputType;
                if (isInput)
                {
                    registerInput(ctx, parentContext);
                }
                else
                {
                    register(ctx, parentContext);
                }
            }
        }

        for (Method method : javaType.getMethods())
        {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            final GraphQLField annotation = method.getAnnotation(GraphQLField.class);
            if (parameterTypes.length > 0 && annotation != null)
            {
                final Class<?> returnType = method.getReturnType();

                if (!Enum.class.isAssignableFrom(returnType) && getGraphQLScalarFor(returnType, null) == null)
                {
                    TypeContext ctx = new TypeContext(parentContext, method);
                    boolean isInput = complexType instanceof InputType;
                    if (isInput)
                    {
                        registerInput(ctx, parentContext);
                    }
                    else
                    {
                        register(ctx, parentContext);
                    }
                }
            }
        }
    }

    public OutputType lookup(Class<?> javaType, TypeContext ctx)
    {
        DomainQL.ensurePojoType(javaType);

        return outputTypes.get(ctx);
    }


    public Collection<GraphQLScalarType> getScalarTypes()
    {
        return Collections.unmodifiableCollection(scalarTypeByName.values());
    }


    public Collection<InputType> getInputTypes()
    {
        return inputTypes.values();
    }

    public GraphQLScalarType getGraphQLScalarFor(Class<?> cls, GraphQLField inputAnno)
    {
        if (inputAnno != null && inputAnno.type().length() > 0)
        {
            return scalarTypeByName.get(inputAnno.type());
        }
        return scalarTypeByClass.get(cls);
    }
}
