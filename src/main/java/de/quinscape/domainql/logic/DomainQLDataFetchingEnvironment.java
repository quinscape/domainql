package de.quinscape.domainql.logic;

import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionId;
import graphql.execution.ExecutionTypeInfo;
import graphql.language.Field;
import graphql.language.FragmentDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;

import java.util.List;
import java.util.Map;

/**
 * Wraps the the standard GraphQL DataFetchingEnvironment to add DomainQL specific members.
 */
public class DomainQLDataFetchingEnvironment
    implements DataFetchingEnvironment
{
    private final DataFetchingEnvironment env;

    private final Class<?> typeParam;

    public DomainQLDataFetchingEnvironment(DataFetchingEnvironment env, Class<?> typeParam)
    {
        this.env = env;
        this.typeParam = typeParam;
    }


    @Override
    public <T> T getSource()
    {
        return env.getSource();
    }


    @Override
    public Map<String, Object> getArguments()
    {
        return env.getArguments();
    }


    @Override
    public boolean containsArgument(String name)
    {
        return env.containsArgument(name);
    }


    @Override
    public <T> T getArgument(String name)
    {
        return env.getArgument(name);
    }


    @Override
    public <T> T getContext()
    {
        return env.getContext();
    }


    @Override
    public <T> T getRoot()
    {
        return env.getRoot();
    }


    @Override
    public GraphQLFieldDefinition getFieldDefinition()
    {
        return env.getFieldDefinition();
    }


    @Override
    public List<Field> getFields()
    {
        return env.getFields();
    }


    @Override
    public Field getField()
    {
        return env.getField();
    }


    @Override
    public GraphQLOutputType getFieldType()
    {
        return env.getFieldType();
    }


    @Override
    public ExecutionTypeInfo getFieldTypeInfo()
    {
        return env.getFieldTypeInfo();
    }


    @Override
    public GraphQLType getParentType()
    {
        return env.getParentType();
    }


    @Override
    public GraphQLSchema getGraphQLSchema()
    {
        return env.getGraphQLSchema();
    }


    @Override
    public Map<String, FragmentDefinition> getFragmentsByName()
    {
        return env.getFragmentsByName();
    }


    @Override
    public ExecutionId getExecutionId()
    {
        return env.getExecutionId();
    }


    @Override
    public DataFetchingFieldSelectionSet getSelectionSet()
    {
        return env.getSelectionSet();
    }


    @Override
    public ExecutionContext getExecutionContext()
    {
        return env.getExecutionContext();
    }

    /// DOMAINQL SPECIFIC METHODS

    public Class<?> getTypeParam()
    {
        return typeParam;
    }
}
