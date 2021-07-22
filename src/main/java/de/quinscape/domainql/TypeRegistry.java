package de.quinscape.domainql;

import de.quinscape.domainql.annotation.GraphQLComputed;
import de.quinscape.domainql.annotation.GraphQLField;
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
import java.sql.Date;
import java.sql.Timestamp;
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
            final String name = scalarType.getName();
            final GraphQLScalarType existing = map.put(name, scalarType);
            if (existing != null && existing != scalarType)
            {
                throw new DomainQLTypeException(
                    "Scalar name '" + name + "' is declared by both " +
                        scalarType + " (" + scalarType.getClass().getName() + ") and " +
                        existing + " (" + existing.getClass().getName() + "). " +
                        "Did you forget to rename a copy&pasted scalar?"
                );
            }
        }

        return map;
    }



    public InputType registerInput(TypeContext typeContext)
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

        final Collection<Class<?>> actualTypeArguments = typeContext.getTypeArguments();
        for (Class<?> cls : actualTypeArguments)
        {
            registerInput(new TypeContext(typeContext, cls));
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
        final Class<?> javaType = ctx.getType();
        
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

        final Collection<Class<?>> actualTypeArguments = ctx.getTypeArguments();

        for (Class<?> cls : actualTypeArguments)
        {
            register(new TypeContext(ctx, cls));
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
            final GraphQLComputed computedAnno = JSONUtil.findAnnotation(info, GraphQLComputed.class);

            if (!DomainQL.isNormalProperty(info) || type.isArray() || computedAnno != null)
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
                    registerInput(ctx);
                }
                else
                {
                    register(ctx);
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
                        registerInput(ctx);
                    }
                    else
                    {
                        register(ctx);
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


    /**
     * Checks a given POJO type for override by resolving its simple name again.
     *
     * @param pojoClass     POJO type to check
     *
     * @return overriding type or identical type
     */
    public Class<?> getOutputOverride(Class<?> pojoClass)
    {
        final OutputType outputType = lookup(pojoClass.getSimpleName());
        return outputType != null ? outputType.getJavaType() : null;
    }
}
