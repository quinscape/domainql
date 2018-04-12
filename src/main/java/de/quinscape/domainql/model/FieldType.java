package de.quinscape.domainql.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public enum FieldType
{
    STRING(String.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    BIG_DECIMAL(BigDecimal.class),
    BOOLEAN(Boolean.class),
    TIMESTAMP(Timestamp.class),
    DATE(Date.class),
    UUID(String.class),
    CUSTOM_SQL(Object.class);

    private final Class<?> javaType;


    FieldType(Class<?> javaType)
    {
        this.javaType = javaType;
    }


    public Class<?> getJavaType()
    {
        return javaType;
    }

    public static FieldType getFieldType(Class<?> cls)
    {
        for (FieldType fieldType : FieldType.values())
        {
            if (fieldType.getJavaType().isAssignableFrom(cls))
            {
                return fieldType;
            }
        }

        throw new IllegalStateException("Unhandled java type: " + cls.getName());
    }
}
