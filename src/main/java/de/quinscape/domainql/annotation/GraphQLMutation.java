package de.quinscape.domainql.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a graphql mutation method.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GraphQLMutation
{
    /**
     * Name of the graphql mutation. If left empty (the default), the method name is used.
     * @return
     */
    String value() default "";

    String description() default "";
}
