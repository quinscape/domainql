package de.quinscape.domainql;

import de.quinscape.domainql.annotation.ResolvedGenericType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Encapsulates a type reference which might be either a simple java type of a degenerified generic type based on a
 * raw type and concrete type variable substitutions.
 * 
 */
public final class TypeContext
{
    private final static Logger log = LoggerFactory.getLogger(TypeContext.class);

    /**
     * Raw context type
     */
    private final Class<?> type;

    /**
     * Actual type arguments (same indexes as {@link #typeVars})
     */
    private final Type[] actualTypeArguments;

    /**
     * Type variables (same indexes as {@link #actualTypeArguments})
     */
    private final TypeVariable[] typeVars;

    /**
     * Name of the corresponding GraphQL type.
     */
    private final String typeName;

    /**
     * Parent type context.
     */
    private final TypeContext parent;


    public TypeContext(TypeContext parent, Class<?> type)
    {
        this.parent = parent;
        this.type = type;
        actualTypeArguments = null;
        typeVars = null;
        typeName = buildTypeName();
    }

    public TypeContext(TypeContext parent, Class<?> type, Type genericType, ResolvedGenericType anno)
    {
        if (genericType instanceof ParameterizedType)
        {
            final ParameterizedType parameterizedType = (ParameterizedType) genericType;

            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            final TypeVariable[] typeVars = ((Class) parameterizedType.getRawType()).getTypeParameters();

            this.type = type;
            this.actualTypeArguments = actualTypeArguments;
            this.typeVars = typeVars;
        }
        else
        {
            this.type = type;
            this.actualTypeArguments = null;
            this.typeVars = null;
        }

        //final ResolvedGenericType anno = method.getAnnotation(ResolvedGenericType.class);
        this.parent = parent;
        if (anno != null && anno.value().length() > 0)
        {
            this.typeName = anno.value();
        }
        else
        {
            this.typeName = buildTypeName();
        }
    }

    public TypeContext(TypeContext parent, Method m)
    {
        this(
            parent,
            m.getReturnType(),
            m.getGenericReturnType(),
            m.getAnnotation(ResolvedGenericType.class)
        );
    }

    public Class<?> getFirstActualType()
    {
        if (actualTypeArguments == null || actualTypeArguments.length == 0)
        {
            return null;
        }
        else
        {
            final Type actualTypeArgument = actualTypeArguments[0];

            if (!(actualTypeArgument instanceof Class))
            {
                throw new IllegalStateException("Argument is not a  class: " + actualTypeArguments);
            }

            return (Class<?>) actualTypeArgument;
        }
    }


    public Type[] getActualTypeArguments()
    {
        return actualTypeArguments;
    }


    public Class<?> getType()
    {
        return type;
    }


    public Class<?> resolveType(String name)
    {
        if (typeVars == null || actualTypeArguments == null)
        {
            return null;
        }

        for (int i = 0; i < typeVars.length; i++)
        {
            TypeVariable typeVar = typeVars[i];
            if (typeVar.getName().equals(name))
            {
                final Type typeArg = actualTypeArguments[i];

                if (typeArg instanceof TypeVariable)
                {
                    return parent.resolveType(((TypeVariable) typeArg).getName());
                }
                else
                {
                    return (Class<?>) typeArg;
                }

            }
        }
        return null;
    }


    public String getTypeName()
    {
        return typeName;
    }

    private String buildTypeName()
    {
        if (typeVars == null || actualTypeArguments == null)
        {
            return type.getSimpleName();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(type.getSimpleName());
        for (Type actualTypeArgument : actualTypeArguments)
        {
            final Class<?> cls;
            Class<?> resolved;
            if (actualTypeArgument instanceof Class)
            {
                cls = (Class<?>) actualTypeArgument;
            }
            else if (actualTypeArgument instanceof ParameterizedType)
            {
                cls = (Class<?>) ((ParameterizedType) actualTypeArgument).getRawType();
            }
            else if (actualTypeArgument instanceof TypeVariable)
            {
                resolved = resolveTypeInternal(((TypeVariable) actualTypeArgument).getName());
                if (resolved != null)
                {
                    cls = resolved;
                }
                else if (parent != null)
                {
                    cls = parent.resolveType(((TypeVariable) actualTypeArgument).getName());
                    if (cls == null)
                    {
                        throw new IllegalStateException("Cannot handle non-concreate class type argument: " + this);
                    }
                }
                else
                {
                    throw new IllegalStateException("Cannot handle non-concreate class type argument: " + this);
                }
            }
            else
            {
                throw new IllegalStateException("Cannot handle non-concreate class type argument: " + this);
            }
            sb.append(cls.getSimpleName());
        }

        return sb.toString();
    }


    private Class<?> resolveTypeInternal(String name)
    {
        Class<?> cls = resolveType(name);
        if (cls == null && parent != null)
        {
            return parent.resolveType(name);
        }
        return cls;
    }


    public TypeContext getParent()
    {
        return parent;
    }


    @Override
    public boolean equals(Object o)
    {
        // comparing just the final type name is the easiest way to compare
        // considering all the factors of raw type, type var resolution
        // including resolution from the parent chain

        if (this == o)
        {
            return true;
        }

        if (o instanceof TypeContext)
        {
            TypeContext that = (TypeContext) o;
            return Objects.equals(this.typeName, that.typeName);

        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return 17 + typeName.hashCode() * 37;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "type = " + type
            + ", actualTypeArguments = " + Arrays.toString(actualTypeArguments)
            + ", typeVars = " + Arrays.toString(typeVars)
            + ", typeName = '" + typeName + '\''
            ;
    }


    /**
     * Returns a clear text java-ish description of the type context
     * @return clear text java-ish description of the type context
     */
    public String describe()
    {
        if (actualTypeArguments == null)
        {
            return type.getSimpleName();
        }

        StringBuilder buff = new StringBuilder();

        buff.append(type.getSimpleName())
            .append('<');

        for (int i = 0; i < actualTypeArguments.length; i++)
        {
            if (i > 0)
            {
                buff.append(",");
            }


            Type actualTypeArgument = actualTypeArguments[i];

            if (actualTypeArgument instanceof TypeVariable)
            {
                final Class<?> cls = resolveTypeInternal(((TypeVariable) actualTypeArgument).getName());
                buff.append(cls.getSimpleName());
            }
            else if (actualTypeArgument instanceof Class)
            {
                buff.append(((Class) actualTypeArgument).getSimpleName());
            }
            else
            {
                buff.append(
                    actualTypeArgument.getTypeName()
                );
            }
        }

        buff.append('>');

        return buff.toString();
    }
}
