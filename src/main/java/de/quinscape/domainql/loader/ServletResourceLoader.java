package de.quinscape.domainql.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Loads servlet context resources. If the servlet context can be resolved to an actual file system path,
 * the loader with use hot-reloadable {@link FileResourceHandle}s, otherwise it will use {@link StreamResourceHandle}s.
 */
public class ServletResourceLoader
    implements ResourceChangeListener, ResourceLoader
{
    private final static Logger log = LoggerFactory.getLogger(ServletResourceLoader.class);


    private final ServletContext servletContext;

    private final String resourcePath;

    private final boolean useFileAccess;

    private final Java7NIOResourceWatcher watchDir;

    private final String basePath;

    private ConcurrentMap<String, ResourceHandle<?>> resourceHandles = new ConcurrentHashMap<>();

    public ServletResourceLoader(ServletContext servletContext, String resourcePath, boolean recursive) throws IOException
    {

        this.servletContext = servletContext;

        this.basePath = servletContext.getRealPath(resourcePath);
        useFileAccess = basePath != null;

        if (log.isInfoEnabled())
        {
            log.info(
                "Creating {} ServletResourceLoader for resource path '{}' (recursive = {})",
                    useFileAccess ? "hot-reload" : "stream",
                    resourcePath,
                    recursive
            );
        }

        if (useFileAccess)
        {
            this.resourcePath = null;

            final File baseDir = new File(basePath);
            if (!baseDir.exists())
            {
                throw new IllegalStateException(basePath + " does not exist");
            }

            if (!baseDir.isDirectory())
            {
                throw new IllegalStateException(basePath + " is not a directory");
            }

            watchDir = new Java7NIOResourceWatcher(basePath, recursive);
            watchDir.register(this);
            watchDir.start();
        }
        else
        {
            this.resourcePath = resourcePath.endsWith("/") ? resourcePath : resourcePath + "/";
            watchDir = null;
        }
    }



    @Override
    public <T> ResourceHandle<T> getResourceHandle(String path, ResourceConverter<T> loader)
    {
        if (path == null)
        {
            throw new IllegalArgumentException("path can't be null");
        }

        if (loader == null)
        {
            throw new IllegalArgumentException("loader can't be null");
        }

        ResourceHandle<T> handle;

        if (useFileAccess)
        {
            handle = new FileResourceHandle(new File(basePath , path.replace('/', File.separatorChar)), loader);
        }
        else
        {
            handle = new StreamResourceHandle(servletContext, resourcePath + path, loader);
        }

        final ResourceHandle<T> existing = (ResourceHandle<T>) resourceHandles.putIfAbsent(path, handle);
        if (existing != null)
        {
            return existing;
        }
        else
        {
            return handle;
        }
    }


    @Override
    public void shutDown()
    {
        if (watchDir != null)
        {
            log.info("Shutting down");

            watchDir.shutDown();
        }
    }


    @Override
    public void onResourceChange(ResourceEvent resourceEvent, String rootPath, String resourcePath)
    {
        log.debug("Resource Event: {} {}", resourceEvent, resourcePath);

        final ResourceHandle<?> handle = resourceHandles.get(resourcePath);
        if (handle != null)
        {
            handle.flush();
        }
    }
}
