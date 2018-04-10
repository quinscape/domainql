package de.quinscape.domainql;

public class DomainQLBuilderException
    extends RuntimeException
{
    private static final long serialVersionUID = -6107054351550261974L;


    public DomainQLBuilderException(String message)
    {
        super(message);
    }


    public DomainQLBuilderException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public DomainQLBuilderException(Throwable cause)
    {
        super(cause);
    }


    public DomainQLBuilderException(
        String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace
    )
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
