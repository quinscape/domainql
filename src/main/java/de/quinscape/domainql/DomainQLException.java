package de.quinscape.domainql;

public class DomainQLException
    extends RuntimeException
{
    private static final long serialVersionUID = -3518149341721548644L;


    public DomainQLException(String message)
    {
        super(message);
    }


    public DomainQLException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public DomainQLException(Throwable cause)
    {
        super(cause);
    }
}
