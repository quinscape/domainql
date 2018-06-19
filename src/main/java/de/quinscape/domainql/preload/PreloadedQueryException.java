package de.quinscape.domainql.preload;

import de.quinscape.domainql.DomainQLException;

public class PreloadedQueryException
    extends DomainQLException
{
    private static final long serialVersionUID = -4767509426529378025L;


    public PreloadedQueryException(String message)
    {
        super(message);
    }


    public PreloadedQueryException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public PreloadedQueryException(Throwable cause)
    {
        super(cause);
    }
}
