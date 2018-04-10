package de.quinscape.domainql.logic;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.DomainQLException;
import de.quinscape.domainql.annotation.GraphQLInput;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.param.ParameterProvider;
import de.quinscape.domainql.param.ParameterProviderFactory;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Analyzes annotated LogicBeans and finds queries and mutations.
 */
public class LogicBeanAnalyzer
{
    private final static Logger log = LoggerFactory.getLogger(LogicBeanAnalyzer.class);


    private final Set<Query> queries = new LinkedHashSet<>();

    private final Set<Mutation> mutations = new LinkedHashSet<>();

    private final DomainQL domainQL;

    private final Collection<ParameterProviderFactory> parameterProviderFactories;

    private final Map<Class<?>, String> inputTypes;


    public LogicBeanAnalyzer(
        DomainQL domainQL,
        Collection<ParameterProviderFactory> parameterProviderFactories,
        Collection<Object> logicBeans,
        Map<Class<?>, String> inputTypes
    )
    {
        this.domainQL = domainQL;
        this.parameterProviderFactories = parameterProviderFactories;
        this.inputTypes = inputTypes;
        logicBeans.forEach(this::discover);
    }


    public Set<Query> getQueries()
    {
        return queries;
    }


    public Set<Mutation> getMutations()
    {
        return mutations;
    }


    public void discover(Object logicBean)
    {
        if (logicBean instanceof TargetSource)
        {
            try
            {
                logicBean = ((TargetSource) logicBean).getTarget();
            }
            catch (Exception e)
            {
                throw new DomainQLException("Error accessing spring target source", e);
            }
        }

        final Class<?> cls = AopProxyUtils.ultimateTargetClass(logicBean);
        log.debug("Analyzing logic bean {}", cls.getName());

        final MethodAccess methodAccess = MethodAccess.get(cls);


        for (Method method : cls.getMethods())
        {
            final String methodName = method.getName();
            final Class<?>[] parameterTypes = method.getParameterTypes();

            final GraphQLQuery queryAnno = method.getDeclaredAnnotation(GraphQLQuery.class);
            final GraphQLMutation mutationAnno = method.getDeclaredAnnotation(GraphQLMutation.class);


            final String locationInfo = logicBean.getClass().getName() + ":" + methodName;
            if (queryAnno != null)
            {
                final GraphQLOutputType resultType = getGraphQLOutputType(locationInfo, method);
                final String name = queryAnno.value().length() > 0 ? queryAnno.value() : methodName;
                final int methodIndex = methodAccess.getIndex(methodName, parameterTypes);
                log.debug("QUERY {}", name);

                final List<ParameterProvider> parameterProviders = createParameterProviders(
                    locationInfo,
                    method.getParameters()
                );

                final Query query = new Query(
                    name.length() > 0 ? name : methodName,
                    queryAnno.description(),
                    logicBean,
                    methodAccess,
                    methodIndex,
                    parameterProviders,
                    resultType
                );
                queries.add(
                    query
                );

            }

            if (mutationAnno != null)
            {

                final String name = mutationAnno.value().length() > 0 ? mutationAnno.value() : methodName;
                final int methodIndex = methodAccess.getIndex(methodName, parameterTypes);

                log.debug("MUTATION {}", name);

                final GraphQLOutputType resultType = getGraphQLOutputType(locationInfo, method);

                final List<ParameterProvider> parameterProviders = createParameterProviders(
                    locationInfo,
                    method.getParameters()
                );
                final Mutation mutation = new Mutation(
                    name.length() > 0 ? name : methodName,
                    mutationAnno.description(),
                    logicBean,
                    methodAccess,
                    methodIndex,
                    parameterProviders,
                    resultType
                );
                mutations.add(
                    mutation
                );
            }
        }
    }


    private GraphQLOutputType getGraphQLOutputType(String locationInfo, Method method)
    {
        final Class<?> returnType = method.getReturnType();

        final GraphQLOutputType resultType;
        if (returnType == null || returnType.equals(Void.TYPE))
        {
            resultType = null;
        }
        else
        {
            if (List.class.isAssignableFrom(returnType))
            {
                final Type genericReturnType = method.getGenericReturnType();
                if (!(genericReturnType instanceof ParameterizedType))
                {
                    throw new DomainQLException(locationInfo + ": List return type must be parametrized.");
                }

                final Class<?> elementClass = (Class<?>) ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];

                final GraphQLOutputType elementType = domainQL.getOutputType(elementClass);
                if (elementType == null)
                {
                    throw new IllegalStateException(locationInfo + ": Cannot resolve GraphQL output type for element type " + elementClass.getName());
                }

                resultType = new GraphQLList(elementType);
            }
            else
            {
                resultType = domainQL.getOutputType(returnType);
                if (resultType == null)
                {
                    throw new IllegalStateException(locationInfo + ": Cannot resolve GraphQL output type for " + returnType.getName());
                }
            }

        }
        return resultType;
    }


    private List<ParameterProvider> createParameterProviders(
        String name, Parameter[] parameters
    )
    {
        try
        {
            List<ParameterProvider> list = new ArrayList<>(parameters.length);
            for (int i = 0, parameterTypesLength = parameters.length; i < parameterTypesLength; i++)
            {
                final Parameter parameter = parameters[i];
                Class<?> parameterType = parameter.getType();
                final Annotation[] parameterAnnotations = parameter.getDeclaredAnnotations();

                final GraphQLInput argAnno = parameter.getDeclaredAnnotation(GraphQLInput.class);

                if ((argAnno == null || argAnno.value().length() == 0) && !parameter.isNamePresent())
                {
                    throw new IllegalStateException(name +
                        ": Cannot determine Method parameter name due to metadata not provided by Java compiler and " +
                        "no @GraphQLArgument annotation being defined. " +
                        "Either you need to define @GraphQLArgument annotations for all logic parameters or you need " +
                        "to enable parameter metadata in your java compiler. " +
                        "For maven this is the property maven.compiler.parameters=true, for the javac command line " +
                        "compiler it is the -parameters option.");
                }

                for (ParameterProviderFactory factory : parameterProviderFactories)
                {
                    final ParameterProvider provider;
                    provider = factory.createIfApplicable(parameterType, parameterAnnotations);
                    if (provider != null)
                    {
                        list.add(provider);
                        log.debug("-- {}", provider);
                    }
                    else
                    {
                        GraphQLInputType inputType = DomainQL.getGraphQLScalarFor(parameterType);
                        if (inputType == null)
                        {
                            final String nameFromConfig = inputTypes.get(parameterType);
                            if (nameFromConfig != null)
                            {
                                inputType = new GraphQLTypeReference(nameFromConfig);
                            }
                            else
                            {
                                final String newInputName = parameterType.getSimpleName();
                                inputTypes.put(parameterType, newInputName);
                                inputType = new GraphQLTypeReference(newInputName);
                            }

                        }

                        boolean isRequired = argAnno != null && argAnno.required();
                        final NotNull notNullAnno = parameter.getAnnotation(NotNull.class);
                        boolean jpaRequired = notNullAnno != null;


                        if (jpaRequired && !isRequired)
                        {
                            throw new DomainQLException(name +
                                ": Required field disagreement between @NotNull and @GraphQLInput required value");
                        }


                        final String parameterName = argAnno != null && argAnno.value().length() > 0 ? argAnno.value() : parameter.getName();
                        final String description = argAnno != null ? argAnno.description() : null;
                        final Object defaultValue;

                        final Object defaultValueFromAnno = argAnno != null ? argAnno.defaultValue() : null;
                        if (String.class.isAssignableFrom(parameterType))
                        {
                            defaultValue = defaultValueFromAnno;
                        }
                        else
                        {
                            defaultValue = ConvertUtils.convert(defaultValueFromAnno, parameterType);
                        }

                        final GraphQLValueProvider graphQLValueProvider = new GraphQLValueProvider(
                            parameterName,
                            description,
                            isRequired ? GraphQLNonNull.nonNull(inputType) : inputType,
                            defaultValue
                        );

                        final String paramDesc = graphQLValueProvider.getDescription();
                        log.debug("  {}", graphQLValueProvider.getArgumentName() + ": " + graphQLValueProvider.getInputType().getName() + (StringUtils.hasText(paramDesc) ? " # " + paramDesc : ""));

                        list.add(
                            graphQLValueProvider
                        );
                    }
//                        else
//                        {
//                            throw new IllegalStateException(
//                                name + ": Cannot provide value for parameter type " + parameterType.getClass()
//                                    .getName() + ". No ParameterProviderFactory was able to create a " +
//                                    "ParameterProvider and no GraphGLType was defined for that class.");
//                        }
//                    }
                }
            }
            return list;
        }
        catch (Exception e)
        {
            throw new DomainQLException(e);
        }
    }
}
