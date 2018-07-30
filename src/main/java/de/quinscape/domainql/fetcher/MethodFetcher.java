package de.quinscape.domainql.fetcher;

import com.esotericsoftware.reflectasm.MethodAccess;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import org.apache.commons.beanutils.ConvertUtils;

import java.util.List;
import java.util.Map;

public class MethodFetcher
    implements DataFetcher<Object>
{


    private final MethodAccess methodAccess;

    private final int methodIndex;

    private final List<String> parameterNames;

    private final Class<?>[] parameterTypes;


    public MethodFetcher(
        MethodAccess methodAccess, int methodIndex, List<String> parameterNames, Class<?>[] parameterTypes
    )
    {

        this.methodAccess = methodAccess;
        this.methodIndex = methodIndex;
        this.parameterNames = parameterNames;
        this.parameterTypes = parameterTypes;
    }


    @Override
    public Object get(DataFetchingEnvironment environment)
    {
        final Object[] parameters = getParameters(environment);
        return methodAccess.invoke(environment.getSource(), methodIndex, parameters);
    }


    private Object[] getParameters(DataFetchingEnvironment environment)
    {
        final Map<String, Object> arguments = environment.getArguments();

        final List<GraphQLArgument> gqlArgs = environment.getFieldDefinition().getArguments();

        Object[] params = new Object[parameterNames.size()];

        for (int i = 0; i < parameterNames.size(); i++)
        {
            final String name = parameterNames.get(i);
            final Object value = arguments.get(name);
            if (parameterTypes[i].isInstance(value))
            {
                params[i] = value;
            }
            else
            {
                params[i] = ConvertUtils.convert(value, parameterTypes[i]);
            }
        }
        return params;
    }
}
