package de.quinscape.domainql.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures an input field.
 *
 * Applicable to Input type property methods and GraphQL logic query or mutator parameters.
 */

@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GraphQLInput
{
    /**
     * Name of the input field. If left empty (the default), the property name of the method is used.
     */
    String value() default "";

    /**
     * Description for the field.
     */
    String description() default "";

    /**
     * Default value for the field.
     */
    String defaultValue() default "";

    boolean required() default false;
}
