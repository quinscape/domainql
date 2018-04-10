package de.quinscape.domainql.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a graphql query method.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GraphQLQuery
{
    /**
     * Name of the graphql query. If left empty (the default), the method name is used.
     *
     * @return name or empty
     */
    String value() default "";

    /**
     * Description for this query.
     *
     * @return description
     */
    String description() default "";
}
