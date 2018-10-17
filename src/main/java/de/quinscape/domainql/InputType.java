package de.quinscape.domainql;

public class InputType
    implements ComplexType
{

    private final String name;

    private final TypeContext typeContext;

    private final boolean isEnum;


    public InputType(
        String name,
        TypeContext typeContext
    )
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }

        if (typeContext == null)
        {
            throw new IllegalArgumentException("typeContext can't be null");
        }


        this.name = name;
        this.typeContext = typeContext;
        this.isEnum = Enum.class.isAssignableFrom(typeContext.getType());
    }


    public String getName()
    {
        return name;
    }


    @Override
    public TypeContext getTypeContext()
    {
        return typeContext;
    }


    public Class<?> getJavaType()
    {
        return typeContext.getType();
    }


    public boolean isEnum()
    {
        return isEnum;
    }
}
