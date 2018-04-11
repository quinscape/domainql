package de.quinscape.domainql;

public class DomainQLTypeException
    extends DomainQLException
{
    private static final long serialVersionUID = -4246712981720721765L;


    public DomainQLTypeException(String message)
    {
        super(message);
    }


    public DomainQLTypeException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public DomainQLTypeException(Throwable cause)
    {
        super(cause);
    }
}
