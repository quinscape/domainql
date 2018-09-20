package de.quinscape.domainql;

public class InputType
    implements ComplexType
{

    private final String name;

    private final TypeContext typeContext;

    private final Class<?> javaType;

    private final boolean isEnum;


    public InputType(
        String name,
        TypeContext typeContext,
        Class<?> javaType
    )
    {
        this.name = name;
        this.typeContext = typeContext;
        this.javaType = javaType;
        this.isEnum = Enum.class.isAssignableFrom(javaType);
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
        return javaType;
    }


    public boolean isEnum()
    {
        return isEnum;
    }
}
