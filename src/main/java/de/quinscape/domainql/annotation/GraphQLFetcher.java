package de.quinscape.domainql.annotation;

import graphql.schema.DataFetcher;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures a property in an output object to use an alternate data fetcher implementation.
 *
 * The implementation constructor must accept the JSON property name and the data value as two string parameters.
 *
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GraphQLFetcher
{
    /**
     * Data Fetcher class implementation
     */
    Class<? extends DataFetcher> value();

    /**
     * User data passed along
     */
    String data() default "";
}

