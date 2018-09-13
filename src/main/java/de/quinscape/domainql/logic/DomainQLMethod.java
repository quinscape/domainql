package de.quinscape.domainql.logic;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.domainql.DomainQLExecutionContext;
import de.quinscape.domainql.DomainQLExecutionException;
import de.quinscape.domainql.param.ParameterProvider;
import graphql.language.Directive;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLDirective;
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

    protected final boolean full;

    protected final Object logicBean;

    protected final MethodAccess methodAccess;

    protected final int methodIndex;

    protected final GraphQLOutputType resultType;

    protected final List<ParameterProvider> parameterProviders;


    public DomainQLMethod(
        String name,
        String description,
        boolean full,
        Object logicBean,
        MethodAccess methodAccess,
        int methodIndex,
        List<ParameterProvider> parameterProviders,
        GraphQLOutputType resultType
    )
    {
        this.name = name;
        this.description = description;
        this.full = full;
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

            final Object value = parameterProvider.provide(environment);
            paramValues[i] = value;
        }
        final Object result = methodAccess.invoke(logicBean, methodIndex, paramValues);

        if (full)
        {
            ensureFullDirective(environment);

            final DomainQLExecutionContext context = environment.getContext();
            if (context == null)
            {
                throw new DomainQLExecutionException("Cannot execute @full " + this.getClass().getSimpleName() + " '" + name + ": A new de.quinscape.domainql.DomainQLExecutionContext instance must be provided as .context() in the GraphQL endpoint.");
            }
            context.setResponse(result);

            return true;
        }
        return result;
    }


    private void ensureFullDirective(DataFetchingEnvironment environment)
    {


        final List<Directive> directives = environment.getField().getDirectives();
        boolean found = false;
        for (Directive directive : directives)
        {
            if (directive.getName().equals("full"))
            {
                found = true;
            }
        }

        if (!found)
        {
            throw new DomainQLExecutionException(this.getClass().getSimpleName() + " '" + name + "' is annotated with (full=true) and cannot be queried without @full");
        }
    }
}
