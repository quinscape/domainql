package de.quinscape.domainql;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.domainql.annotation.GraphQLField;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.annotation.GraphQLQuery;
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

    private final TypeRegistry typeRegistry;

    public LogicBeanAnalyzer(
        DomainQL domainQL,
        Collection<ParameterProviderFactory> parameterProviderFactories,
        Collection<Object> logicBeans,
        TypeRegistry typeRegistry
    )
    {
        this.domainQL = domainQL;
        this.parameterProviderFactories = parameterProviderFactories;
        this.typeRegistry = typeRegistry;
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


                final GraphQLOutputType resultType = getGraphQLOutputType(locationInfo, method);
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


                final Query query = new Query(
                    name,
                    queryAnno.description(),
                    queryAnno.full(),
                    logicBean,
                    methodAccess,
                    methodIndex,
                    parameterProviders,
                    queryAnno.full() ? Scalars.GraphQLBoolean : resultType
                );
                queries.add(
                    query
                );
            }

            final GraphQLMutation mutationAnno = method.getDeclaredAnnotation(GraphQLMutation.class);
            if (mutationAnno != null)
            {

                final String name = mutationAnno.value().length() > 0 ? mutationAnno.value() : methodName;
                final int methodIndex = methodAccess.getIndex(methodName, parameterTypes);

                log.debug("MUTATION {}", name);

                if (mutationAnno.full() && !domainQL.isFullSupported())
                {
                    throw new IllegalStateException("Mutation " + name + " cannot declare full: DomainQL service not configured to support @full");
                }

                final GraphQLOutputType resultType = getGraphQLOutputType(locationInfo, method);

                if (resultType == null)
                {
                    throw new IllegalStateException("Could not find type for " + locationInfo + ", method = " + method);
                }

                final List<ParameterProvider> parameterProviders = createParameterProviders(
                    locationInfo,
                    method
                );
                final Mutation mutation = new Mutation(
                    name.length() > 0 ? name : methodName,
                    mutationAnno.description(),
                    mutationAnno.full(),
                    logicBean,
                    methodAccess,
                    methodIndex,
                    parameterProviders,
                    mutationAnno.full() ? Scalars.GraphQLBoolean : resultType
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

        final TypeContext ctx = new TypeContext(null, method);

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

            final Type[] actualTypeArguments = ctx.getActualTypeArguments();
            if (List.class.isAssignableFrom(returnType))
            {
                if (actualTypeArguments == null)
                {
                    throw new IllegalStateException(locationInfo + ": List return type must be parametrized.");
                }


                final Class<?> elementClass = ctx.getFirstActualType();
                final GraphQLScalarType scalar = DomainQL.getGraphQLScalarFor(elementClass, fieldAnno);
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
                final Type genericParameterType = genericParameterTypes[i];
                Class<?> parameterType = parameter.getType();
                final Annotation[] parameterAnnotations = parameter.getDeclaredAnnotations();

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

                ParameterProvider provider = null;
                for (ParameterProviderFactory factory : parameterProviderFactories)
                {
                    provider = invokeFactory(parameterType, parameterAnnotations, factory);

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
                    final TypeContext paramTypeContext = new TypeContext(
                        null,
                        parameter.getType(), genericParameterType, genericTypeAnno);
                    GraphQLInputType inputType = DomainQL.getGraphQLScalarFor(parameterType, argAnno);
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


                            final GraphQLScalarType scalar = DomainQL.getGraphQLScalarFor(elementClass, argAnno);
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
                            final InputType parameterInputType = typeRegistry.registerInput(paramTypeContext);
                            final String nameFromConfig = parameterInputType.getName();
                            inputType = GraphQLTypeReference.typeRef(nameFromConfig);
                        }
                    }

                    boolean isRequired = argAnno != null && argAnno.notNull();
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
                        typeRegistry
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
//        }
//        catch (Exception e)
//        {
//            throw new DomainQLException(e);
//        }
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
