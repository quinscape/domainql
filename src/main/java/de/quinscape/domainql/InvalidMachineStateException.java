package de.quinscape.domainql;

public class InvalidMachineStateException
    extends DomainQLException
{
    private static final long serialVersionUID = 7078932465087727423L;


    public InvalidMachineStateException(String message)
    {
        super(message);
    }


    public InvalidMachineStateException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InvalidMachineStateException(Throwable cause)
    {
        super(cause);
    }
}
