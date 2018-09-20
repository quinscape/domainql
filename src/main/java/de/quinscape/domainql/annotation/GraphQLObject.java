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
    String name() default "";

    /**
     * Whether to create an input mirror for this output object. Default is true which is the same behavior as having
     * not GraphQLObject annotation.
     *
     * @return  whether to create a input mirror
     */
    boolean createMirror() default true;


    /**
     * Enables GraphQL auto-generation of concrete types for a generic type on the server side.
     * <p>
     *     If we have a class
     * </p>
     * <pre>
     * \@GraphQLObject(degenerify = true)
     * public class Paged&lt;T&gt;
     * {
     *     // ...
     * }
     * </pre>
     *
     * each use of that class in a query or mutation method will be replaced with a degenerified type.
     * <pre>
     * \@GraphQLQuery
     * public Paged&lt;Foo&gt; foos(/* ... *\/)
     * {
     *     // ...
     * }
     * </pre>
     *
     * above Query will use a generified "PagedFoo" type 
     *
     * @return true if type is to be degenerified
     */
    boolean degenerify() default false;
}
