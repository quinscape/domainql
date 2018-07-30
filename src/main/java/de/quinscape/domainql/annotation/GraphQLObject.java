package de.quinscape.domainql.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures an output object.
 *
 * Applicable to output objects
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GraphQLObject
{
    /**
     * Whether to create an input mirror for this output object. Default is true which is the same behavior as having
     * not GraphQLObject annotation.
     *
     * @return  whether to create a input mirror
     */
    boolean createMirror() default true;
}
