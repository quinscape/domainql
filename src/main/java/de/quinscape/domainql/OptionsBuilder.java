package de.quinscape.domainql;

import de.quinscape.domainql.config.Options;
import org.atteo.evo.inflector.English;

import java.util.function.Function;

/**
 * Mutable builder class for {@link Options}.
 */
public class OptionsBuilder
{

    private DomainQLBuilder domainQLBuilder;


    OptionsBuilder(DomainQLBuilder domainQLBuilder)
    {
        this.domainQLBuilder = domainQLBuilder;

    }

    private boolean useDatabaseFieldNames;

    private String foreignKeySuffix = "Id";

    private Function<String, String> pluralizationFunction = English::plural;


    public boolean isUseDatabaseFieldNames()
    {
        return useDatabaseFieldNames;
    }

    /**
     * Set this to <code>true</code> to have exactly the same field names in database and GraphQL schema.
     *
     * Default is using the lower camelcase property names created by JOOQ. ( "customer_id" =&gt; "customerId")
     *
     * @param useDatabaseFieldNames <code>true</code> to use database field names as is.
     *
     * @return this builder
     */
    public OptionsBuilder useDatabaseFieldNames(boolean useDatabaseFieldNames)
    {
        this.useDatabaseFieldNames = useDatabaseFieldNames;
        return this;
    }


    public String getForeignKeySuffix()
    {
        return foreignKeySuffix;
    }


    /**
     * Mandatory suffix for foreign key fields that gets cut off to form the object link or M to N fetcher names.
     *
     * @param foreignKeySuffix  suffix (default is "Id")
     *
     * @return this builder
     */
    public OptionsBuilder foreignKeySuffix(String foreignKeySuffix)
    {
        this.foreignKeySuffix = foreignKeySuffix;
        return this;
    }


    public Function<String, String> getPluralizationFunction()
    {
        return pluralizationFunction;
    }


    /**
     * Function to use in cases where we conclude from the name of column to the plural collection name for
     * the M to N fields created for it.
     *
     * Default is using the evo-inflector library.
     *
     * @param pluralizationFunction     Function that produces a plural of a word
     *
     * @return this builder
     */
    public OptionsBuilder pluralizationFunction(Function<String, String> pluralizationFunction)
    {
        this.pluralizationFunction = pluralizationFunction;
        return this;
    }

    Options buildOptions()
    {
        return new Options(
            useDatabaseFieldNames,
            foreignKeySuffix,
            pluralizationFunction
        );
    }


    public DomainQL build()
    {
        return endOptions().build();
    }


    private DomainQLBuilder endOptions()
    {
        return domainQLBuilder;
    }
}
