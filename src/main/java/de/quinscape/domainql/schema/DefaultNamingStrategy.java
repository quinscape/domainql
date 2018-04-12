package de.quinscape.domainql.schema;

import java.util.List;

public class DefaultNamingStrategy
    implements NamingStrategy
{
    @Override
    public String getTableName(String typeName)
    {
        return camelCaseToUnderline(typeName);
    }


    static String camelCaseToUnderline(String name)
    {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < name.length(); i++)
        {
            char c = name.charAt(i);
            if (Character.isUpperCase(c))
            {
                if (i > 0 && !Character.isUpperCase(name.charAt(i-1)))
                {
                    buf.append("_");
                }
                buf.append(Character.toLowerCase(c));
            }
            else
            {
                buf.append(c);
            }
        }
        return buf.toString();
    }


    @Override
    public String[] getFieldName(String typeName, String propertyName)
    {
        return new String[] { getTableName(typeName), camelCaseToUnderline(propertyName) };
    }


    @Override
    public String getForeignKeyName(String typeName, String targetType, List<String> targetProperty)
    {
        StringBuilder buff = new StringBuilder();
        for (String name : targetProperty)
        {
            buff.append('_').append(name);
        }
        return "fk_" + typeName + camelCaseToUnderline(targetType + buff.toString());
    }


    @Override
    public String getUniqueConstraintName(String typeName, String propertyName)
    {
        return "uc_" + camelCaseToUnderline(typeName + "_" + propertyName);
    }


    @Override
    public String getPrimaryKeyName(String typeName)
    {
        return "pk_" + camelCaseToUnderline(typeName);
    }
}
