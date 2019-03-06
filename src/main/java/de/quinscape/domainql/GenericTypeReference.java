package de.quinscape.domainql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Encapsulates a single usage of a degenerified type
 */
public final class GenericTypeReference
{
    private final String type;
    private final String genericType;

    private final List<String> typeParameters;


    public GenericTypeReference(
        String type,
        String genericType,
        List<String> typeParameters
    )
    {
        this.typeParameters = typeParameters;
        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }

        if (genericType == null)
        {
            throw new IllegalArgumentException("genericType can't be null");
        }

        if (typeParameters == null || typeParameters.size() == 0)
        {
            throw new IllegalArgumentException("typeParameters can't be null or empty");
        }

        this.type = type;
        this.genericType = genericType;
    }


    public String getType()
    {
        return type;
    }


    public String getGenericType()
    {
        return genericType;
    }



    /**
     * Creates a new generic type reference from the given type context.
     *
     *
     * @param typeRegistry      type registry
     * @param typeContext       type context
     *
     * @return  generic type reference or <code>null</code> if the type context is not a parametrized context.
     */
    public static GenericTypeReference create(TypeRegistry typeRegistry, TypeContext typeContext)
    {
        if (!typeContext.isParametrized())
        {
            return null;
        }
        return new GenericTypeReference(
            typeContext.getTypeName(),
            typeContext.getType().getName(),
            resolveTypeArguments(typeRegistry, typeContext)
        );
    }


    public List<String> getTypeParameters()
    {
        return typeParameters;
    }


    /**
     * Resolves a list of classes to the corresponding GraphQL types registered for it.
     *
     * @param typeRegistry      type registry
     * @param typeContext       parametrized type context
     *
     * @return List of type arguments as GraphQL types
     */
    private static List<String> resolveTypeArguments(
        TypeRegistry typeRegistry,
        TypeContext typeContext
    )
    {
        final Collection<Class<?>> typeArguments = typeContext.getTypeArguments();
        List<String> types = new ArrayList<>(typeArguments.size());

        for (Class<?> cls : typeArguments)
        {
            final OutputType outputType = typeRegistry.lookup(cls);
            if (outputType == null)
            {
                throw new IllegalStateException("Cannot lookup " + cls + " / " + typeContext);
            }
            types.add( outputType.getName());
        }
        return types;
    }
}
