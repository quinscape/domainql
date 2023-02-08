package de.quinscape.domainql;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.domainql.annotation.GraphQLField;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.annotation.GraphQLTypeParam;
import de.quinscape.domainql.annotation.ResolvedGenericType;
import de.quinscape.domainql.logic.GraphQLValueProvider;
import de.quinscape.domainql.logic.Mutation;
import de.quinscape.domainql.logic.Query;
import de.quinscape.domainql.param.ParameterProvider;
import de.quinscape.domainql.param.ParameterProviderFactory;
import graphql.Scalars;
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

import jakarta.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Analyzes annotated LogicBeans and finds queries and mutations.
 */
class LogicBeanAnalyzer
{

    private final static Logger log = LoggerFactory.getLogger(LogicBeanAnalyzer.class);


    private final Set<Query> queries = new LinkedHashSet<>();

    private final Set<Mutation> mutations = new LinkedHashSet<>();

    private final DomainQL domainQL;

    private final Collection<ParameterProviderFactory> parameterProviderFactories;

    private final TypeRegistry typeRegistry;

    private final Map<String,Class<?>> outputTypeOverrides;

    LogicBeanAnalyzer(
        DomainQL domainQL,
        Collection<ParameterProviderFactory> parameterProviderFactories,
        Collection<Object> logicBeans,
        TypeRegistry typeRegistry
    )
    {
        this.domainQL = domainQL;
        this.parameterProviderFactories = parameterProviderFactories;
        this.typeRegistry = typeRegistry;
        this.outputTypeOverrides = new HashMap<>();

        logicBeans.forEach(this::discover);
    }


    Set<Query> getQueries()
    {
        return queries;
    }


    Set<Mutation> getMutations()
    {
        return mutations;
    }


    private void discover(Object logicBean)
    {
        final Class<?> cls = AopProxyUtils.ultimateTargetClass(logicBean);
        log.debug("Analyzing logic bean {}", cls.getName());

        final MethodAccess methodAccess = MethodAccess.get(cls);


        for (Method method : cls.getMethods())
        {
            final String methodName = method.getName();
            final Class<?>[] parameterTypes = method.getParameterTypes();

            final GraphQLQuery queryAnno = method.getDeclaredAnnotation(GraphQLQuery.class);

            final String locationInfo = logicBean.getClass().getName() + ":" + methodName;
            if (queryAnno != null)
            {
                queries.addAll(
                    buildQueries(
                        logicBean,
                        methodAccess,
                        method,
                        methodName,
                        parameterTypes,
                        queryAnno,
                        locationInfo
                    )
                );
            }

            final GraphQLMutation mutationAnno = method.getDeclaredAnnotation(GraphQLMutation.class);
            if (mutationAnno != null)
            {
                mutations.addAll(
                    buildMutations(
                        logicBean,
                        methodAccess,
                        method,
                        methodName,
                        parameterTypes,
                        locationInfo,
                        mutationAnno
                    )
                );
            }
        }
    }

    private final static String NAME_PATTERN_WILDCARD = "*";

    private List<Query> buildQueries(
        Object logicBean,
        MethodAccess methodAccess,
        Method method,
        String methodName,
        Class<?>[] parameterTypes,
        GraphQLQuery queryAnno,
        String locationInfo
    )
    {
        final String name = queryAnno.value().length() > 0 ? queryAnno.value() : methodName;
        final int methodIndex = methodAccess.getIndex(methodName, parameterTypes);
        log.debug("QUERY {}", name);

        if (queryAnno.full() && !domainQL.isFullSupported())
        {
            throw new IllegalStateException("Query " + name + " cannot declare full: DomainQL service not configured to support @full");
        }

        final List<ParameterProvider> parameterProviders = createParameterProviders(
            locationInfo,
            method
        );

        final Parameter typeParamAnnoParameter = findTypeParamAnnoParameter(method);

        if (typeParamAnnoParameter != null)
        {
            if (!typeParamAnnoParameter.getType().equals(Class.class))
            {
                throw new IllegalStateException(locationInfo + ": @GrapQLTypeParam parameter must be of type Class<T>");
            }

            final GraphQLTypeParam typeParamAnno = typeParamAnnoParameter.getAnnotation(GraphQLTypeParam.class);

            final Type actualTypeArgument =
                ((ParameterizedType) typeParamAnnoParameter.getParameterizedType()).getActualTypeArguments()[0];

            if (!(actualTypeArgument instanceof TypeVariable))
            {
                throw new IllegalStateException(locationInfo + ": @GrapQLTypeParam parameter must be declared as type variable ( e.g. Class<T> ) ");
            }

            final List<Query> list = new ArrayList<>();

            final String namePattern = typeParamAnno.namePattern().length() == 0 ? methodName + NAME_PATTERN_WILDCARD : typeParamAnno.namePattern();
            if (!namePattern.contains(NAME_PATTERN_WILDCARD))
            {
                throw new IllegalStateException(locationInfo + ": @GraphQLTypeParam: namePattern must contain a '*' wildcard");
            }

            for (Class typeParam : typeParamAnno.types())
            {

                final String varName = ((TypeVariable) actualTypeArgument).getName();
                Map<String, Class<?>> map = new LinkedHashMap<>();
                map.put(varName, typeParam);

                final TypeContext newContext = new TypeContext(null, method.getReturnType(), map);
                final GraphQLOutputType outputType = getGraphQLOutputType(
                    locationInfo,
                    method.getReturnType(),
                    null,
                    newContext
                );
                list.add(
                    new Query(
                        domainQL,
                        namePattern.replace(NAME_PATTERN_WILDCARD, typeParam.getSimpleName()),
                        queryAnno.description(),
                        queryAnno.full(),
                        logicBean,
                        methodAccess,
                        methodIndex,
                        parameterProviders,
                        queryAnno.full() ? Scalars.GraphQLBoolean : outputType,
                        newContext,
                        methodName
                    )
                );
            }
            return list;
        }
        else
        {
            final GraphQLOutputType resultType = getGraphQLOutputType(locationInfo, method);

            return Collections.singletonList(
                new Query(
                    domainQL,
                    name,
                    queryAnno.description(),
                    queryAnno.full(),
                    logicBean,
                    methodAccess,
                    methodIndex,
                    parameterProviders,
                    queryAnno.full() ? Scalars.GraphQLBoolean : resultType,
                    null,
                    null
                )
            );
        }
    }


    private Parameter findTypeParamAnnoParameter(Method method)
    {
        for (Parameter parameter : method.getParameters())
        {
            final GraphQLTypeParam anno = parameter.getAnnotation(GraphQLTypeParam.class);
            if (anno != null)
            {
                return parameter;
            }
        }

        return null;
    }


    private List<Mutation> buildMutations(
        Object logicBean,
        MethodAccess methodAccess,
        Method method,
        String methodName,
        Class<?>[] parameterTypes,
        String locationInfo,
        GraphQLMutation mutationAnno
    )
    {
        final String name = mutationAnno.value().length() > 0 ? mutationAnno.value() : methodName;
        final int methodIndex = methodAccess.getIndex(methodName, parameterTypes);

        log.debug("MUTATION {}", name);

        if (mutationAnno.full() && !domainQL.isFullSupported())
        {
            throw new IllegalStateException("Mutation " + name +
                " cannot declare full: DomainQL service not configured to support @full");
        }
        final List<ParameterProvider> parameterProviders = createParameterProviders(
            locationInfo,
            method
        );
        final Parameter typeParamAnnoParameter = findTypeParamAnnoParameter(method);

        if (typeParamAnnoParameter != null)
        {
            if (!typeParamAnnoParameter.getType().equals(Class.class))
            {
                throw new IllegalStateException(locationInfo + ": @GrapQLTypeParam parameter must be of type Class<T>");
            }

            final GraphQLTypeParam typeParamAnno = typeParamAnnoParameter.getAnnotation(GraphQLTypeParam.class);

            final Type actualTypeArgument =
                ((ParameterizedType) typeParamAnnoParameter.getParameterizedType()).getActualTypeArguments()[0];

            if (!(actualTypeArgument instanceof TypeVariable))
            {
                throw new IllegalStateException(locationInfo + ": @GrapQLTypeParam parameter must be declared as type variable ( e.g. Class<T> ) ");
            }

            final List<Mutation> list = new ArrayList<>();

            final String namePattern = typeParamAnno.namePattern().length() == 0 ? methodName + NAME_PATTERN_WILDCARD : typeParamAnno.namePattern();
            if (!namePattern.contains(NAME_PATTERN_WILDCARD))
            {
                throw new IllegalStateException(locationInfo + ": @GraphQLTypeParam: namePattern must contain a '*' wildcard");
            }

            for (Class typeParam : typeParamAnno.types())
            {
                final String varName = ((TypeVariable) actualTypeArgument).getName();
                Map<String, Class<?>> map = new LinkedHashMap<>();
                map.put(varName, typeParam);

                final TypeContext newContext = new TypeContext(null, method.getReturnType(), map);
                final GraphQLOutputType outputType = getGraphQLOutputType(
                    locationInfo,
                    method.getReturnType(),
                    null,
                    newContext
                );
                list.add(
                    new Mutation(
                        domainQL,
                        namePattern.replace(NAME_PATTERN_WILDCARD, typeParam.getSimpleName()),
                        mutationAnno.description(),
                        mutationAnno.full(),
                        logicBean,
                        methodAccess,
                        methodIndex,
                        parameterProviders,
                        mutationAnno.full() ? Scalars.GraphQLBoolean : outputType,
                        newContext,
                        methodName
                    )
                );
            }
            return list;
        }
        else
        {
            final GraphQLOutputType resultType = getGraphQLOutputType(locationInfo, method);

            return Collections.singletonList(
                new Mutation(
                    domainQL,
                    name,
                    mutationAnno.description(),
                    mutationAnno.full(),
                    logicBean,
                    methodAccess,
                    methodIndex,
                    parameterProviders,
                    mutationAnno.full() ? Scalars.GraphQLBoolean : resultType,
                    null,
                    null
                )
            );
        }
    }


    private GraphQLOutputType getGraphQLOutputType(String locationInfo, Method method)
    {
        final Class<?> returnType = method.getReturnType();

        final GraphQLField fieldAnno = method.getAnnotation(GraphQLField.class);

        final TypeContext ctx = new TypeContext(null, method);

        return getGraphQLOutputType(locationInfo, returnType, fieldAnno, ctx);
    }


    private GraphQLOutputType getGraphQLOutputType(
        String locationInfo,
        Class<?> returnType,
        GraphQLField fieldAnno,
        TypeContext ctx
    )
    {
        final GraphQLOutputType resultType;
        if (returnType == null || returnType.equals(Void.TYPE))
        {
            throw new DomainQLException(locationInfo + ": Logic methods must return an output type");
        }
        else
        {

            if (fieldAnno != null && fieldAnno.value().length() > 0)
            {
                throw new DomainQLException(locationInfo + ": Return values can't have names");
            }

            final GraphQLScalarType scalarType = domainQL.getTypeRegistry().getGraphQLScalarFor(returnType, fieldAnno);
            if (scalarType != null)
            {
                return scalarType;
            }


            if (List.class.isAssignableFrom(returnType))
            {
                if (!ctx.isParametrized())
                {
                    throw new IllegalStateException(locationInfo + ": List return type must be parametrized.");
                }


                final Class<?> elementClass = ctx.getFirstActualType();
                final GraphQLScalarType scalar = domainQL.getTypeRegistry().getGraphQLScalarFor(elementClass, fieldAnno);
                if (scalar != null)
                {
                    resultType = new GraphQLList(scalar);
                }
                else
                {
                    // create new type context for the element of the generic list
                    final OutputType outputType = typeRegistry.register(new TypeContext(null,  elementClass));
                    resultType = new GraphQLList(new GraphQLTypeReference(outputType.getName()));
                }
            }
            else if (Enum.class.isAssignableFrom(returnType))
            {
                final OutputType enumType = typeRegistry.register(ctx);

                resultType = new GraphQLTypeReference(enumType.getName());
            }
            else
            {
                if (returnType.isInterface())
                {
                    throw new DomainQLException("Cannot handle " + returnType);
                }

                final OutputType outputType = typeRegistry.register(ctx);
                resultType = new GraphQLTypeReference(outputType.getName());
            }
        }
        return resultType;
    }


    private List<ParameterProvider> createParameterProviders(
        String name, Method method
    )
    {
//        try
//        {
            final Parameter[] parameters = method.getParameters();
            final Type[] genericParameterTypes = method.getGenericParameterTypes();

            List<ParameterProvider> list = new ArrayList<>(parameters.length);
            for (int i = 0, parameterTypesLength = parameters.length; i < parameterTypesLength; i++)
            {
                final Parameter parameter = parameters[i];
                ParameterProvider provider = createValueProvider(name, genericParameterTypes[i], parameter);

                list.add(
                    provider
                );
            }
            return list;
//        }
//        catch (Exception e)
//        {
//            throw new DomainQLException(e);
//        }
    }


    private ParameterProvider createValueProvider(String name, Type genericParameterType, Parameter parameter)
    {
        Class<?> parameterType = parameter.getType();
        final Annotation[] parameterAnnotations = parameter.getDeclaredAnnotations();


        ParameterProvider provider = null;
        for (ParameterProviderFactory factory : parameterProviderFactories)
        {
            provider = invokeFactory(parameterType, parameterAnnotations, factory);

            if (provider != null)
            {
                break;
            }
        }

        if (provider == null)
        {
            provider = createValueProvider(
                name,
                parameter,
                genericParameterType,
                parameterType
            );
        }
        return provider;
    }


    private ParameterProvider createValueProvider(
        String name,
        Parameter parameter,
        Type genericParameterType,
        Class<?> parameterType
    )
    {

        final GraphQLField argAnno = parameter.getDeclaredAnnotation(GraphQLField.class);
        final ResolvedGenericType genericTypeAnno = parameter.getDeclaredAnnotation(ResolvedGenericType.class);

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

        ParameterProvider provider;
        final TypeContext paramTypeContext = new TypeContext(
            null,
            parameterType,
            genericParameterType,
            genericTypeAnno
        );

        GraphQLInputType inputType = domainQL.getTypeRegistry().getGraphQLScalarFor(parameterType, argAnno);
        if (inputType == null)
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


                final GraphQLScalarType scalar = domainQL.getTypeRegistry().getGraphQLScalarFor(elementClass, argAnno);
                if (scalar != null)
                {
                    inputType = new GraphQLList(scalar);
                }
                else
                {
                    InputType newInputType = typeRegistry.registerInput(
                        // create new type context for the element of the generic list.
                        new TypeContext(paramTypeContext, elementClass)
                    );
                    inputType = new GraphQLList(new GraphQLTypeReference(newInputType.getName()));
                }
            }
            else
            {
                if (parameterType.isInterface())
                {
                    throw new DomainQLException("Cannot handle " + parameterType);
                }


                final InputType parameterInputType = typeRegistry.registerInput(paramTypeContext);
                final String nameFromConfig = parameterInputType.getName();
                inputType = GraphQLTypeReference.typeRef(nameFromConfig);
            }
        }

        final boolean fieldAnnoPresent = argAnno != null;
        final NotNull notNullAnno = parameter.getAnnotation(NotNull.class);
        boolean jpaNotNull = notNullAnno != null;


        if (jpaNotNull && fieldAnnoPresent && !argAnno.notNull())
        {
            throw new DomainQLException(name +
                ": Required field disagreement between @NotNull and @GraphQLField required value");
        }

        final boolean isNotNull = jpaNotNull || (fieldAnnoPresent && argAnno.notNull());

        final String parameterName = fieldAnnoPresent && argAnno.value().length() > 0 ? argAnno
            .value() : parameter.getName();
        final String description = fieldAnnoPresent ? argAnno.description() : null;
        final Object defaultValue;

        final Object defaultValueFromAnno = fieldAnnoPresent ? argAnno.defaultValue() : null;
        if (String.class.isAssignableFrom(parameterType))
        {
            defaultValue = defaultValueFromAnno;
        }
        else
        {
            defaultValue = defaultValueFromAnno != null ? ConvertUtils.convert(defaultValueFromAnno, parameterType) : null;
        }

        final GraphQLValueProvider graphQLValueProvider = new GraphQLValueProvider(
            parameterName,
            description,
            isNotNull,
            inputType,
            defaultValue,
            typeRegistry,
            parameterType
        );

        final String paramDesc = graphQLValueProvider.getDescription();
        log.debug("  {}", graphQLValueProvider.getArgumentName() + ": " + graphQLValueProvider
            .getInputType() + (StringUtils.hasText(paramDesc) ? " # " + paramDesc : ""));


        provider = graphQLValueProvider;
        return provider;
    }


    private ParameterProvider invokeFactory(
        Class<?> parameterType, Annotation[] parameterAnnotations, ParameterProviderFactory factory
    )
    {
        try
        {
            return factory.createIfApplicable(parameterType, parameterAnnotations);
        }
        catch (Exception e)
        {
            throw new DomainQLException(e);
        }
    }
}
