package de.quinscape.domainql;

/**
 * DomainQL specific execution context. For now only needed to store @full responses
 */
public class DomainQLExecutionContext
{
    private Object response;


    public Object getResponse()
    {
        return response;
    }

    public void setResponse(Object response)
    {
        this.response = response;
    }
}
