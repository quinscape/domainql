package de.quinscape.domainql.logic;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.param.ParameterProvider;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLInputType;

public class GraphQLValueProvider
    implements ParameterProvider<Object>
{
    private final DomainQL domainQL;

    private final String argumentName;

    private final String description;

    private final GraphQLInputType inputType;

    private final Object defaultValue;


    public GraphQLValueProvider(
        DomainQL domainQL,
        String argumentName,
        String description,
        GraphQLInputType inputType,
        Object defaultValue
    )
    {
        this.domainQL = domainQL;
        this.argumentName = argumentName;
        this.description = description;
        this.inputType = inputType;
        this.defaultValue = defaultValue;
    }


    @Override
    public Object provide(DataFetchingEnvironment environment)
    {
        return environment.getArgument(argumentName);
    }


    public String getArgumentName()
    {
        return argumentName;
    }


    public String getDescription()
    {
        return description;
    }


    public Object getDefaultValue()
    {
        return defaultValue;
    }


    public GraphQLInputType getInputType()
    {
        return inputType;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "argumentName = '" + argumentName + '\''
            + ", description = '" + description + '\''
            + ", inputType = " + inputType
            + ", defaultValue = " + defaultValue
            ;
    }
}
