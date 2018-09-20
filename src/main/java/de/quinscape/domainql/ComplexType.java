package de.quinscape.domainql;

public interface ComplexType
{
    String getName();

    TypeContext getTypeContext();

    Class<?> getJavaType();

    boolean isEnum();
}
