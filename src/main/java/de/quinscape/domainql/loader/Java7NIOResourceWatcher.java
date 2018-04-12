package de.quinscape.domainql.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implements a module watcher based on Java7 watch services.
 *
 */
public class Java7NIOResourceWatcher
    extends WatchDir
    implements ResourceWatcher, Runnable
{
    private final static Logger log = LoggerFactory.getLogger(Java7NIOResourceWatcher.class);

    private final int basePathLength;

    private final String path;

    private CopyOnWriteArrayList<ResourceChangeListener> listeners = new CopyOnWriteArrayList<>();

    private volatile boolean disabled;


    public Java7NIOResourceWatcher(String path, boolean recursive) throws IOException
    {
        super(path, recursive);
        this.path = path;
        this.basePathLength = path.length();
    }

    public void start()
    {
        log.info("Start watcher for {}", path);

        Thread t = new Thread(this, "Watcher-" + path);
        t.setDaemon(true);
        t.start();
    }


    @Override
    public void run()
    {
        processEvents();
    }

    @Override
    protected void onWatchEvent(ResourceEvent resourceEvent, File child)
    {
//        if (disabled)
//        {
//            return;
//        }

        String path = child.getPath();
        final String resourcePath = path.substring(basePathLength);
        for (ResourceChangeListener listener : listeners)
        {
            log.debug("Signaling {} for {} ({})", listener, path, resourceEvent);

            listener.onResourceChange(resourceEvent, path, resourcePath);
        }
    }


    @Override
    public void register(ResourceChangeListener listener)
    {
        log.debug("{}: Register {}", this, listener);

        listeners.add(listener);
    }


    @Override
    public void clearListeners()
    {
        log.debug("Clear all listeners");
        listeners.clear();
    }


    @Override
    public void enable()
    {
        this.disabled = false;
    }


    @Override
    public void disable()
    {
        this.disabled = true;
    }
}
