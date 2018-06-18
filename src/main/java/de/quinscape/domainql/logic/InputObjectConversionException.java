package de.quinscape.domainql.logic;

import de.quinscape.domainql.DomainQLException;

public class InputObjectConversionException
    extends DomainQLException
{

    private static final long serialVersionUID = -5509707385416798594L;


    public InputObjectConversionException(String message)
    {
        super(message);
    }


    public InputObjectConversionException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InputObjectConversionException(Throwable cause)
    {
        super(cause);
    }
}
