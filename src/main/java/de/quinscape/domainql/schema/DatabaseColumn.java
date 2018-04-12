package de.quinscape.domainql.schema;

/**
 * Encapsulates the an existing column taken from information_schema.columns
 *
 */
public class DatabaseColumn
{
    private final String dataType;
    private final Integer characterMaximumLength;
    private final boolean nullable;

    public DatabaseColumn(String dataType, Integer characterMaximumLength, boolean nullable)
    {
        this.dataType = dataType;
        this.characterMaximumLength = characterMaximumLength;
        this.nullable = nullable;
    }

    public boolean isNullable()
    {
        return nullable;
    }

    public Integer getCharacterMaximumLength()
    {
        return characterMaximumLength;
    }

    public String getDataType()
    {
        return dataType;
    }
}
