package de.quinscape.domainql.logic;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.domainql.param.ParameterProvider;
import graphql.schema.GraphQLOutputType;

import java.util.List;

/**
 * Internal configuration for a mutation type.
 */
public class Mutation
    extends DomainQLMethod
{
    public Mutation(
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
        super(name, description, full, logicBean, methodAccess, methodIndex, parameterProviders, resultType);

    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            + ", description = '" + description + '\''
            + ", parameterProviders = " + parameterProviders
            ;
    }
}
