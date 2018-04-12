package de.quinscape.domainql.loader;

import java.io.IOException;

/**
 * Handle for a single resource.
 *
 * @param <T>   type of content
 */
public interface ResourceHandle<T>
{
    /**
     * Returns <code>true</code> if this resource handle represents resource that can be written back to.
     *
     * @return <code>true</code> if writable
     */
    boolean isWritable();

    /**
     * Returns the contents of this handle. Might be <code>null</code> to indicate a missing resource.
     * <p>
     *     If this handle is backed by hot-reload capable ResourceLoader, the contents returned will
     *     reflect the current state of the file.
     * </p>
     *
     * @return  contents or <code>null</code>
     * @throws IOException
     */
    T getContent() throws IOException;

    /**
     *
     * @param newValue
     * @throws IOException
     */
    void update(T newValue) throws IOException;

    void flush();
}
