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
public @interface GraphQLField
{
    /**
     * Name of the input field. If left empty (the default), the property name of the method is used.
     * @return name of the input field or empty to take the name of the underlying property or parameter
     */
    String value() default "";

    /**
     * Description for the field.
     *
     * @return description
     */
    String description() default "";

    /**
     * Default value for the field.
     *
     * @return default value
     */
    String defaultValue() default "";

    /**
     * Scalar type to use, default is to conclude the scalar type from the java type.
     *
     * Useful for e.g. Currency values.
     *
     * @return  type
     */
    String type() default "";

    /**
     * if set to <code>true</code>, the input value will be configured as non-null.
     *
     * @return is required?
     */
    boolean notNull() default false;
}
