package de.quinscape.domainql.generic;

import org.svenson.AbstractDynamicProperties;
import org.svenson.JSONProperty;

import javax.validation.constraints.NotNull;

/**
 * A generic java side implementation of the domain object interface.
 */
public class GenericDomainObject
    extends AbstractDynamicProperties
    implements DomainObject
{

    @Override
    @NotNull
    @JSONProperty(value = DOMAIN_TYPE_PROPERTY, readOnly = true, priority = 1000)
    public String getDomainType()
    {
        return (String) getProperty(DOMAIN_TYPE_PROPERTY);
    }


    @Override
    public void setDomainType(String domainType)
    {
        setProperty(DOMAIN_TYPE_PROPERTY, domainType);
    }

}
