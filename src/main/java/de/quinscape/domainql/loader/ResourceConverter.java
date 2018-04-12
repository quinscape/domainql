package de.quinscape.domainql.loader;

import java.io.InputStream;

/**
 * Implemented by classes converting {@link ResourceLoader} resource streams
 *
 * @param <T>
 */
public interface ResourceConverter<T>
{
    T readStream(InputStream data);
    byte[] toByteArray(T value);
}
