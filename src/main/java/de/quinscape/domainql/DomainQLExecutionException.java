package de.quinscape.domainql;

public class DomainQLExecutionException
    extends DomainQLException
{
    private static final long serialVersionUID = -8343955271035853751L;


    public DomainQLExecutionException(String message)
    {
        super(message);
    }


    public DomainQLExecutionException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public DomainQLExecutionException(Throwable cause)
    {
        super(cause);
    }
}
