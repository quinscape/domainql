package de.quinscape.domainql.logic;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.collect.BiMap;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.DomainQLException;
import de.quinscape.domainql.annotation.GraphQLField;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.param.ParameterProvider;
import de.quinscape.domainql.param.ParameterProviderFactory;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeReference;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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

    private final BiMap<Class<?>, String> inputTypes;

    private final Map<Class<?>, GraphQLOutputType> registeredOutputTypes;

    private final Map<Class<? extends Enum>, GraphQLEnumType> registeredEnumTypes;

    private final Consumer<Class<?>> registerOutputType;


    public LogicBeanAnalyzer(
        DomainQL domainQL,
        Collection<ParameterProviderFactory> parameterProviderFactories,
        Collection<Object> logicBeans,
        BiMap<Class<?>, String> inputTypes,
        Map<Class<?>, GraphQLOutputType> registeredOutputTypes,
        Map<Class<? extends Enum>, GraphQLEnumType> registeredEnumTypes,
        Consumer<Class<?>> registerOutputType
    )
    {
        this.domainQL = domainQL;
        this.parameterProviderFactories = parameterProviderFactories;
        this.inputTypes = inputTypes;
        this.registeredOutputTypes = registeredOutputTypes;
        this.registeredEnumTypes = registeredEnumTypes;
        this.registerOutputType = registerOutputType;
        logicBeans.forEach(this::discover);
    }


    private Map<String, Class<?>> invert(Map<Class<?>, String> inputTypes)
    {
        Map<String, Class<?>> map = new HashMap<>();
        for (Map.Entry<Class<?>, String> e : inputTypes.entrySet())
        {
            map.put(e.getValue(), e.getKey());
        }
        return map;
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
            throw new DomainQLException(locationInfo + ": Logic methods must return an output type");
        }
        else
        {
            final GraphQLField fieldAnno = method.getAnnotation(GraphQLField.class);

            if (fieldAnno != null && fieldAnno.value().length() > 0)
            {
                throw new DomainQLException(locationInfo + ": Return values can't have names");
            }

            final GraphQLScalarType scalarType = DomainQL.getGraphQLScalarFor(returnType, fieldAnno);
            if (scalarType != null)
            {
                return scalarType;
            }

            if (List.class.isAssignableFrom(returnType))
            {
                final Type genericReturnType = method.getGenericReturnType();
                if (!(genericReturnType instanceof ParameterizedType))
                {
                    throw new DomainQLException(locationInfo + ": List return type must be parametrized.");
                }

                final Class<?> elementClass = (Class<?>) ((ParameterizedType) genericReturnType)
                    .getActualTypeArguments()[0];


                final GraphQLScalarType scalar = DomainQL.getGraphQLScalarFor(elementClass, fieldAnno);
                if (scalar != null)
                {
                    resultType = new GraphQLList(scalar);
                }
                else
                {
                    final GraphQLOutputType outputType = registeredOutputTypes.get(elementClass);
                    if (outputType == null)
                    {
                        registerOutputType.accept(elementClass);
                    }

                    resultType = new GraphQLList(new GraphQLTypeReference(elementClass.getSimpleName()));
                }
            }
            else if (Enum.class.isAssignableFrom(returnType))
            {
                GraphQLEnumType enumType = registeredEnumTypes.get(returnType);
                if (enumType == null)
                {
                    enumType = DomainQL.buildEnumType(returnType);
                    registeredEnumTypes.put((Class<? extends Enum>) returnType, enumType);

                }
                resultType = enumType;
            }
            else
            {
                final GraphQLOutputType outputType = registeredOutputTypes.get(returnType);
                if (outputType == null)
                {

                    registerOutputType.accept(returnType);
                }

                resultType = new GraphQLTypeReference(returnType.getSimpleName());
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

                final GraphQLField argAnno = parameter.getDeclaredAnnotation(GraphQLField.class);

                if ((argAnno == null || argAnno.value().length() == 0) && !parameter.isNamePresent())
                {
                    throw new IllegalStateException(name +
                        ": Cannot determine Method parameter name due to metadata not provided by Java compiler and " +
                        "no @GraphQLField annotation being defined. " +
                        "Either you need to define @GraphQLField annotations for all logic parameters or you need " +
                        "to enable parameter metadata in your java compiler. " +
                        "For maven this is the property maven.compiler.parameters=true, for the javac command line " +
                        "compiler it is the -parameters option.");
                }

                ParameterProvider provider = null;
                for (ParameterProviderFactory factory : parameterProviderFactories)
                {
                    provider = factory.createIfApplicable(parameterType, parameterAnnotations);
                    if (provider != null)
                    {
                        break;
                    }
                }

                if (provider != null)
                {
                    list.add(provider);
                    log.debug("-- {}", provider);
                }
                else
                {
                    GraphQLInputType inputType = DomainQL.getGraphQLScalarFor(parameterType, argAnno);
                    if (inputType == null)
                    {
                        final String nameFromConfig = inputTypes.get(parameterType);
                        if (nameFromConfig != null)
                        {
                            inputType = GraphQLTypeReference.typeRef(nameFromConfig);
                        }
                        else
                        {
                            if (List.class.isAssignableFrom(parameterType))
                            {
                                final Type genericReturnType = parameter.getParameterizedType();
                                if (!(genericReturnType instanceof ParameterizedType))
                                {
                                    throw new DomainQLException(parameter + ": List parameter type must be parametrized.");
                                }

                                final Class<?> elementClass = (Class<?>) ((ParameterizedType) genericReturnType)
                                    .getActualTypeArguments()[0];


                                final GraphQLScalarType scalar = DomainQL.getGraphQLScalarFor(elementClass, argAnno);
                                if (scalar != null)
                                {
                                    inputType = new GraphQLList(scalar);
                                }
                                else
                                {
                                    final String newInputName = DomainQL.getInputTypeName(elementClass);
                                    inputTypes.put(elementClass, newInputName);

                                    inputType = new GraphQLList(new GraphQLTypeReference(newInputName));
                                }
                            }
                            else
                            {
                                final String newInputName = DomainQL.getInputTypeName(parameterType);
                                inputTypes.put(parameterType, DomainQL.getInputTypeName(parameterType));
                                inputType = GraphQLTypeReference.typeRef(newInputName);
                            }
                        }

                    }

                    boolean isRequired = argAnno != null && argAnno.required();
                    final NotNull notNullAnno = parameter.getAnnotation(NotNull.class);
                    boolean jpaRequired = notNullAnno != null;


                    if (jpaRequired && !isRequired)
                    {
                        throw new DomainQLException(name +
                            ": Required field disagreement between @NotNull and @GraphQLField required value");
                    }


                    final String parameterName = argAnno != null && argAnno.value().length() > 0 ? argAnno
                        .value() : parameter.getName();
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
                        isRequired,
                        inputType,
                        defaultValue,
                        inputTypes,
                        registeredEnumTypes
                    );

                    final String paramDesc = graphQLValueProvider.getDescription();
                    log.debug("  {}", graphQLValueProvider.getArgumentName() + ": " + graphQLValueProvider
                        .getInputType() + (StringUtils.hasText(paramDesc) ? " # " + paramDesc : ""));

                    list.add(
                        graphQLValueProvider
                    );
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
