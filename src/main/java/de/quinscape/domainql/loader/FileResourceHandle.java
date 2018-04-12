package de.quinscape.domainql.loader;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * A file based, reload capable resource handle.
 *
 * @param <T>   Type of final content
 */
public class FileResourceHandle<T>
    implements ResourceHandle<T>
{
    private final static Object DOES_NOT_EXIST = new Object();

    private final static Logger log = LoggerFactory.getLogger(FileResourceHandle.class);

    private final File file;

    private final ResourceConverter<T> processor;

    /** current content */
    private volatile Object content;

    public FileResourceHandle(
        File file,
        ResourceConverter<T> processor
    )
    {
        log.debug("Create FileResourceHandle for {} (processor = {})", file, processor);

        this.file = file;
        this.processor = processor;
    }


    @Override
    public boolean isWritable()
    {
        return true;
    }


    @Override
    public T getContent() throws IOException
    {
        if (content == null)
        {
            synchronized (this)
            {
                if (content == null)
                {

                    if (!file.exists() || !file.isFile())
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("File {} does not exist", file.getPath(), processor);
                        }

                        content = DOES_NOT_EXIST;
                    }
                    else
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Loading {} (processor = {})", file.getPath(), processor);
                        }

                        final FileInputStream fileInputStream = new FileInputStream(file);
                        content = processor.readStream(fileInputStream);
                    }
                }
            }
        }
        return content != DOES_NOT_EXIST ? (T) content : null;
    }


    @Override
    public void update(T newValue) throws IOException
    {
        FileUtils.writeByteArrayToFile(file, processor.toByteArray(newValue));
    }


    @Override
    public synchronized void flush()
    {
        if (content != null && log.isDebugEnabled())
        {
            log.debug("Flushing content for {}", file.getPath());
        }

        content = null;
    }
}
