package de.quinscape.domainql.generic;

import de.quinscape.domainql.DomainQLException;

public class DomainObjectCreationException
    extends DomainQLException
{
    private static final long serialVersionUID = -494757270609475551L;


    public DomainObjectCreationException(String message)
    {
        super(message);
    }


    public DomainObjectCreationException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public DomainObjectCreationException(Throwable cause)
    {
        super(cause);
    }
}
