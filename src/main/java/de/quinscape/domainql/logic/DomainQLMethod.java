package de.quinscape.domainql.logic;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.domainql.param.ParameterProvider;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLOutputType;

import java.util.List;

/**
 * Abstract base class for @{@link de.quinscape.domainql.annotation.GraphQLLogic} methods.
 */
public abstract class DomainQLMethod
    implements DataFetcher<Object>
{
    protected final String name;

    protected final String description;

    protected final Object logicBean;

    protected final MethodAccess methodAccess;

    protected final int methodIndex;

    protected final List<ParameterProvider> parameterProviders;

    protected final GraphQLOutputType resultType;


    public DomainQLMethod(
        String name,
        String description,
        Object logicBean,
        MethodAccess methodAccess,
        int methodIndex,
        List<ParameterProvider> parameterProviders,
        GraphQLOutputType resultType
    )
    {
        this.name = name;
        this.description = description;
        this.logicBean = logicBean;
        this.methodAccess = methodAccess;
        this.methodIndex = methodIndex;
        this.parameterProviders = parameterProviders;
        this.resultType = resultType;
    }


    public String getDescription()
    {
        return description;
    }


    public List<ParameterProvider> getParameterProviders()
    {
        return parameterProviders;
    }


    public String getName()
    {
        return name;
    }


    public GraphQLOutputType getResultType()
    {
        return resultType;
    }

    @Override
    public Object get(DataFetchingEnvironment environment)
    {
        final Object[] paramValues = new Object[parameterProviders.size()];

        for (int i = 0; i < parameterProviders.size(); i++)
        {
            ParameterProvider parameterProvider = parameterProviders.get(i);
            paramValues[i] = parameterProvider.provide(environment);
        }
        return methodAccess.invoke(logicBean, methodIndex, paramValues);
    }
}
