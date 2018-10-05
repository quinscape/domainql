package de.quinscape.domainql;

public class OutputType
    implements ComplexType
{
    private final TypeContext typeContext;

    private final Class<?> javaType;

    private final boolean isEnum;


    public OutputType(TypeContext typeContext, Class<?> javaType)
    {
        this.typeContext = typeContext;
        this.javaType = javaType;
        isEnum = Enum.class.isAssignableFrom(javaType);
    }


    @Override
    public String getName()
    {
        return typeContext.getTypeName();
    }


    @Override
    public TypeContext getTypeContext()
    {
        return typeContext;
    }

    @Override
    public Class<?> getJavaType()
    {

        return javaType;
    }


    @Override
    public boolean isEnum()
    {
        return isEnum;
    }


    public OutputTypeType getType()
    {
        return null;
    }
}
