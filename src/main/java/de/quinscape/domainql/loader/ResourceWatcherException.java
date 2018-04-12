package de.quinscape.domainql.loader;

import de.quinscape.domainql.DomainQLException;

public class ResourceWatcherException
    extends DomainQLException
{
    private static final long serialVersionUID = -  1626961630936877528L;


    public ResourceWatcherException(String message)
    {
        super(message);
    }


    public ResourceWatcherException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ResourceWatcherException(Throwable cause)
    {
        super(cause);
    }
}
