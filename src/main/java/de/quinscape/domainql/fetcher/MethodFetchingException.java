package de.quinscape.domainql.fetcher;

import de.quinscape.domainql.DomainQLException;

import java.lang.reflect.InvocationTargetException;

public class MethodFetchingException
    extends DomainQLException
{
    private static final long serialVersionUID = -8654277625360267910L;


    public MethodFetchingException(String message)
    {
        super(message);
    }


    public MethodFetchingException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public MethodFetchingException(Throwable cause)
    {
        super(cause);
    }
}
