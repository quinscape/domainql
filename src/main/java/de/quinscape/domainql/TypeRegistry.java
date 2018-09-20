package de.quinscape.domainql;

import de.quinscape.domainql.annotation.GraphQLField;
import de.quinscape.domainql.util.DegenerificationUtil;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.info.JavaObjectPropertyInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeRegistry
{
    private final static Logger log = LoggerFactory.getLogger(TypeRegistry.class);

    private final DomainQL domainQL;


    private Map<TypeKey, InputType> inputTypes = new HashMap<>();

    private Map<TypeKey, OutputType> outputTypes = new HashMap<>();


    public TypeRegistry(DomainQL domainQL)
    {
        this.domainQL = domainQL;
    }


    public InputType registerInput(TypeContext typeContext)
    {

        return registerInput(typeContext, null);
    }


    public InputType registerInput(TypeContext typeContext, TypeContext parentContext)
    {

        final Class<?> javaType = typeContext.getType();
        DomainQL.ensurePojoType(javaType);

        final TypeKey key = new TypeKey(typeContext);

        final InputType existing = inputTypes.get(key);
        if (existing != null)
        {
            return existing;
        }

        if (Enum.class.isAssignableFrom(javaType))
        {
            final InputType enumType = new InputType(javaType.getSimpleName(), typeContext, javaType);
            inputTypes.put(key, enumType);
            return enumType;
        }

        final String inputTypeName;
        if (typeContext.getTypeName().endsWith(DomainQL.INPUT_SUFFIX))
        {
            inputTypeName = typeContext.getTypeName();
        }
        else
        {
            inputTypeName = typeContext.getTypeName() + DomainQL.INPUT_SUFFIX;
        }

        final InputType newType = new InputType(inputTypeName, typeContext, javaType);

        inputTypes.put(key, newType);

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


    public OutputType register(TypeContext ctx)
    {
        return register(ctx, null);
    }


    public OutputType register(TypeContext ctx, TypeContext parentContext)
    {
        final Class<?> javaType = ctx.getType();
        DomainQL.ensurePojoType(javaType);

        final TypeKey key = new TypeKey(ctx);

        final OutputType existing = outputTypes.get(key);
        if (existing != null)
        {
            return existing;
        }

        if (Enum.class.isAssignableFrom(javaType))
        {
            final OutputType enumType = new OutputType(ctx, javaType);
            outputTypes.put(key, enumType);
            return enumType;
        }


        final OutputType newType = new OutputType(ctx, javaType);

        outputTypes.put(key, newType);

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
        TypeKey key = new TypeKey(typeContext);
        return inputTypes.get(key);
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

            if (DomainQL.getGraphQLScalarFor(nextType, graphQLFieldAnno) == null)
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

                if (!Enum.class.isAssignableFrom(returnType) && DomainQL.getGraphQLScalarFor(returnType, null) == null)
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

        final TypeKey key = new TypeKey(ctx);

        return outputTypes.get(key);
    }


    public Collection<InputType> getInputTypes()
    {
        return inputTypes.values();
    }
}
