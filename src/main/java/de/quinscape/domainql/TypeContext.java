package de.quinscape.domainql;

import com.google.common.collect.Maps;
import de.quinscape.domainql.annotation.ResolvedGenericType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Encapsulates a type reference which might be either a simple java type of a degenerified generic type based on a
 * raw type and concrete type variable substitutions.
 */
public final class TypeContext
{
    private final static Logger log = LoggerFactory.getLogger(TypeContext.class);

    /**
     * Raw context type
     */
    private final Class<?> type;

    private final Map<String, Class<?>> typeMap;

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
        typeMap = Collections.emptyMap();
        typeName = buildTypeName(false);
    }


    public TypeContext(TypeContext parent, Class<?> type, Map<String, Class<?>> typeMap)
    {
        this.parent = parent;
        this.typeMap = typeMap;

        if (type.equals(Object.class))
        {
            // this case only happens for parameterized queries/mutations that return the parameter type directly
            // (e.g. public <T> T query(...) )

            // in this case we simply replace the base type Object.class with the first value from the type map and
            // build a non-degenerified name so we don't repeat the parameterized type in its name

            if (typeMap.size() != 1)
            {
                throw new IllegalStateException("Invalid type Object.class without singular type mapping");
            }

            this.type = typeMap.values().iterator().next();
            typeName = buildTypeName(false);
        }
        else
        {
            this.type = type;
            typeName = buildTypeName(true);
        }

    }


    public TypeContext(TypeContext parent, Class<?> type, Type genericType, ResolvedGenericType anno)
    {
        final boolean degenerifyName;
        if (genericType instanceof ParameterizedType)
        {
            final ParameterizedType parameterizedType = (ParameterizedType) genericType;

            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            final TypeVariable[] typeVars = ((Class) parameterizedType.getRawType()).getTypeParameters();

            this.type = type;

            typeMap = new LinkedHashMap<>();
            degenerifyName = true;

            for (int i = 0; i < actualTypeArguments.length; i++)
            {
                final TypeVariable typeVar = typeVars[i];
                final Type typeArg = actualTypeArguments[i];
                Class<?> cls;
                if (typeArg instanceof Class)
                {
                    cls = (Class<?>) typeArg;
                }
                else if (typeArg instanceof TypeVariable)
                {
                    cls = parent.resolveType(((TypeVariable) typeArg).getName());
                }
                else if (typeArg instanceof ParameterizedType)
                {
                    cls = (Class<?>) ((ParameterizedType) typeArg).getRawType();
                }
                else
                {
                    throw new IllegalStateException("Cannot handle non-concreate class type argument: " + this);
                }

                typeMap.put(typeVar.getName(), cls);
            }
        }
        else
        {
            this.type = type;
            typeMap = Collections.emptyMap();
            degenerifyName = false;
        }

        this.parent = parent;
        if (anno != null && anno.value().length() > 0)
        {
            this.typeName = anno.value();
        }
        else
        {
            this.typeName = buildTypeName(degenerifyName);
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
        if (typeMap.isEmpty())
        {
            return null;
        }
        else
        {
            return typeMap.values().iterator().next();
        }
    }


    public Class<?> getType()
    {
        return type;
    }


    public Class<?> resolveType(String name)
    {
        return typeMap.get(name);
    }


    public String getTypeName()
    {
        return typeName;
    }


    private String buildTypeName(boolean degenerifyName)
    {
        if (!degenerifyName)
        {
            return type.getSimpleName();
        }

        StringBuilder sb = new StringBuilder();
        final String baseName = type.getSimpleName();
        sb.append(baseName);

        for (Class<?> cls : typeMap.values())
        {
            sb.append(cls.getSimpleName());
        }
        return sb.toString();
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
        return Objects.hashCode(typeName);
    }





    /**
     * Returns a clear text java-ish description of the type context
     *
     * @return clear text java-ish description of the type context
     */
    public String describe()
    {
        if (typeMap.isEmpty())
        {
            return type.getName();
        }

        StringBuilder buff = new StringBuilder();

        buff.append(type.getName())
            .append('<');

        for (Class<?> cls : typeMap.values())
        {
            buff.append(cls.getSimpleName());
        }

        buff.append('>');

        return buff.toString();
    }


    public boolean isParametrized()
    {
        return !typeMap.isEmpty();
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "type = " + type
            + ", typeMap = " + typeMap
            + ", typeName = '" + typeName + '\''
            + ", parent = " + parent
            ;
    }


    public Collection<Class<?>> getTypeArguments()
    {
        return typeMap.values();
    }
}
