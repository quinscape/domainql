package de.quinscape.domainql.schema;


import de.quinscape.domainql.model.DomainField;
import de.quinscape.domainql.util.ConfigUtil;

/**
 * Internal representation of database column types for our fields.
 *
 */
public enum FieldType
{
    TEXT,
    CHARACTER_VARYING,
    INTEGER,
    BOOLEAN,
    BIGINT,
    DECIMAL,
    TIMESTAMP_WITHOUT_TIME_ZONE,
    DATE;

    private final String sqlName;


    private FieldType()
    {
        this.sqlName = this.name().replace('_', ' ').toLowerCase();
    }


    public String getSqlExpression(RuntimeContext runtimeContext, DomainField propertyModel)
    {
        switch(this)
        {
            case CHARACTER_VARYING:
                final int maxLength = propertyModel.getMaxLength();
                if (maxLength > 0)
                {
                    return sqlName + "(" + maxLength + ")";
                }
                return sqlName;

            case DECIMAL:
                final int decimalPlaces = ConfigUtil.from(propertyModel, "decimalPlaces", 10);
                final int precision = ConfigUtil.from(propertyModel, "precision", 2);

                if (precision == 0)
                {
                    return sqlName;
                }

                // is it a decimal without fractional part and do we have a specified precision?
                if (decimalPlaces == 0)
                {
                    // is it in integer range?
                    if (precision <= 9)
                    {
                        // use 32 bit int
                        return INTEGER.sqlName;
                    }
                    // is it in bigint range?
                    if (precision <= 19)
                    {
                        // use 64 bit int
                        return BIGINT.sqlName;
                    }

                    // use decimal without precision
                    return sqlName;
                }

                final String sql = sqlName + "(" + precision + "," + decimalPlaces + ")";

                if (decimalPlaces > precision)
                {
                    throw new IllegalStateException(
                        "Error getting sql expression for property model " +
                            propertyModel + " ( "+ sql + ") : scale cannot be larger than the precision");
                }
                return sql;
            default:
                return sqlName;
        }
    }
}
