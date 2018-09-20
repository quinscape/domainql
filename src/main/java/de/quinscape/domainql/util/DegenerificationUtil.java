package de.quinscape.domainql.util;

import de.quinscape.domainql.ComplexType;
import de.quinscape.domainql.DomainQLException;
import de.quinscape.domainql.TypeContext;
import de.quinscape.domainql.annotation.ResolvedGenericType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public final class DegenerificationUtil
{
    private final static Logger log = LoggerFactory.getLogger(DegenerificationUtil.class);


    private DegenerificationUtil()
    {
        // no instances
    }

    public static Class<?> getElementType(ComplexType complexType, Method getterMethod)
    {
        final Type genericReturnType = getterMethod.getGenericReturnType();

        if (!(genericReturnType instanceof ParameterizedType))
        {
            throw new DomainQLException(getterMethod+ ": Property getter type must be parametrized.");
        }

        Class<?> nextType;
        final Type actualType = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
        if (actualType instanceof Class)
        {
            nextType = (Class<?>) actualType;
        }
        else if (actualType instanceof TypeVariable)
        {
            final String name = ((TypeVariable) actualType).getName();
            nextType = complexType.getTypeContext().resolveType(name);
            if (nextType == null)
            {
                throw new DomainQLException("Could not resolve generic type from context " + complexType);
            }
        }
        else
        {
            throw new DomainQLException("Error getting generic type for" + actualType + " / " + getterMethod);
        }

        return nextType;
    }


    public static TypeContext getType(
        TypeContext parentContext,
        ComplexType complexType,
        Method getterMethod
    )
    {

        final Type genericReturnType = getterMethod.getGenericReturnType();

        Class<?> nextType;
        if (genericReturnType instanceof TypeVariable)
        {
            final String name = ((TypeVariable) genericReturnType).getName();
            nextType = complexType.getTypeContext().resolveType(name);
            if (nextType == null)
            {
                throw new DomainQLException("Could not resolve generic type from context " + complexType);
            }
            return new TypeContext(parentContext, nextType);
        }
        else if (genericReturnType instanceof ParameterizedType)
        {
            final ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            nextType = (Class<?>) parameterizedType.getRawType();
            return new TypeContext(parentContext, nextType, parameterizedType, getterMethod.getAnnotation(ResolvedGenericType.class));
        }
        else
        {
            nextType = getterMethod.getReturnType();
            return new TypeContext(parentContext, nextType);
        }
    }

}
