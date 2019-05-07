package de.quinscape.domainql.annotation;

import graphql.schema.GraphQLScalarType;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks classes as GraphQL scalar values.
 * <p>
 *     The scalar must be defined with {@link de.quinscape.domainql.DomainQLBuilder#withAdditionalScalar(Class, GraphQLScalarType)}, this annotation
 *     only protects the scalar from being used without it being formally declared as a scalar value. 
 * </p>
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GraphQLScalar
{
}
