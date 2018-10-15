package de.quinscape.domainql.generic;

import com.google.common.collect.Maps;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

import java.util.Map;
import java.util.Set;

/**
 * GraphQL Scalar implementation that wraps another domain type. The value is transmitted as a scalar but does the
 * appropriate conversions for the wrapped domain object.
 * <p>
 * This scalar allows to write GraphQL mutations that accept any of schema domain types. It requires that all domain
 * object
 * beans implement {@link DomainObject}. Configure the {@link DomainObjectGeneratorStrategy} for jooq to automatically
 * make the generated POJOs implement that interface.
 *
 * @see DomainObject
 */
public class DomainObjectScalar
    extends graphql.schema.GraphQLScalarType

{

    public DomainObjectScalar()
    {

        super(
            "DomainObject", "Container for generic domain objects as scalar",
            new Coercing<DomainObject, Map<String, Object>>()
            {
                @Override
                public Map<String, Object> serialize(Object result) throws CoercingSerializeException
                {
                    if (!(result instanceof DomainObject))
                    {
                        throw new IllegalArgumentException(result + " is not an instance of " + DomainObject.class.getName());
                    }

                    final DomainObject domainObject = (DomainObject) result;

                    if (domainObject instanceof GenericDomainObject)
                    {
                        return ((GenericDomainObject) domainObject).contents();
                    }

                    final Set<String> propertyNames = domainObject.propertyNames();
                    Map<String, Object> convertedType = Maps.newHashMapWithExpectedSize(propertyNames.size());

                    for (String fieldName :propertyNames)
                    {
                        convertedType.put(fieldName, domainObject.getProperty(fieldName));
                    }

                    return convertedType;
                }


                @Override
                public DomainObject parseValue(Object input) throws CoercingParseValueException
                {
                    if (!(input instanceof Map))
                    {
                        throw new CoercingParseValueException("Cannot coerce " + input + " to DomainObject");
                    }

                    return new GenericDomainObject(
                        (Map<String, Object>) input
                    );
                }


                @Override
                public DomainObject parseLiteral(Object input) throws CoercingParseLiteralException
                {
                    throw new CoercingParseLiteralException("Cannot coerce DomainObject from literal");
                }
            }
        );
    }
}
