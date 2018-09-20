package de.quinscape.domainql.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation allowing the definition of the type name for dengenerified types. The default behavior is to
 * concatenate generic class name and type arguments (e.g. <code>Paged&lt;Foo&gt;</code> becomes <code>PagedFoo</code>)
 */

@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResolvedGenericType
{
    /**
     * Name of the resolved generic type / the degenerified type.
     */
    String value();

}
