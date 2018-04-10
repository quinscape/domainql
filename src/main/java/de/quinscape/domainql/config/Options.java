package de.quinscape.domainql.config;

import java.util.function.Function;

/**
 * Miscellaneous options for DomainQL
 */
public class Options
{
    private final boolean useDatabaseFieldNames;

    private final String foreignKeySuffix;

    private final Function<String, String> pluralizationFunction;


    public Options(
        boolean useDatabaseFieldNames,
        String foreignKeySuffix,
        Function<String, String> pluralizationFunction
    )
    {
        this.useDatabaseFieldNames = useDatabaseFieldNames;
        this.foreignKeySuffix = foreignKeySuffix;
        this.pluralizationFunction = pluralizationFunction;
    }


    /**
     * If <code>true</code>, DomainQL will use data base field names instead of java property names for the GraphQL fields.
     *
     * @return  use database names?
     */
    public boolean isUseDatabaseFieldNames()
    {
        return useDatabaseFieldNames;
    }


    /**
     * Returns the string function to use to pluralize words in names.
     *
     * @return  pluralization function
     */
    public Function<String, String> getPluralizationFunction()
    {
        return pluralizationFunction;
    }


    /**
     * Suffix all foreign keys are supposed to have and which is cut off to form object/collection field names.
     *
     * @return  suffix
     */
    public String getForeignKeySuffix()
    {
        return foreignKeySuffix;
    }


}
