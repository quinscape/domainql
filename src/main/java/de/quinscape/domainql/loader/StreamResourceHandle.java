package de.quinscape.domainql.loader;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.InputStream;

/**
 * Stream-based, non-reloadable resource handle.
 *
 * @param <T>   Type of final content
 */
public class StreamResourceHandle<T>
    implements ResourceHandle<T>
{
    private final static Logger log = LoggerFactory.getLogger(StreamResourceHandle.class);


    private final ServletContext servletContext;

    private final String path;

    private final ResourceConverter<T> processor;

    private volatile T content;


    public StreamResourceHandle(ServletContext servletContext, String path, ResourceConverter<T> processor)
    {
        log.debug("Create StreamResourceHandle for {} (processor = {})", path, processor);

        this.servletContext = servletContext;
        this.path = path;
        this.processor = processor;
    }


    @Override
    public boolean isWritable()
    {
        return false;
    }


    @Override
    public T getContent()
    {
        if (content == null)
        {
            synchronized (this)
            {
                if (content == null)
                {
                    final InputStream is = servletContext.getResourceAsStream(path);
                    content = processor.readStream(is);
                }
            }
        }
        return content;
    }


    @Override
    public void update(T newValue)
    {
        throw new UnsupportedOperationException("Stream resources should never be updated");
    }


    @Override
    public void flush()
    {
        throw new UnsupportedOperationException("Stream resource handle should never be flushed");
    }
}
