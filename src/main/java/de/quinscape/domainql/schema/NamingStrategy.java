package de.quinscape.domainql.schema;

import java.util.List;

public interface NamingStrategy
{
    String getTableName(String typeName);

    String[] getFieldName(String typeName, String propertyName);

    String getForeignKeyName(String typeName, String propertyName, List<String> targetProperty);

    String getUniqueConstraintName(String typeName, String propertyName);

    String getPrimaryKeyName(String typeName);
}
