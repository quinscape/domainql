package de.quinscape.domainql.config;

import java.util.function.Function;

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


    public boolean isUseDatabaseFieldNames()
    {
        return useDatabaseFieldNames;
    }


    public Function<String, String> getPluralizationFunction()
    {
        return pluralizationFunction;
    }


    public String getForeignKeySuffix()
    {
        return foreignKeySuffix;
    }


}
