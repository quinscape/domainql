package de.quinscape.domainql.generic;

import org.jooq.util.DefaultGeneratorStrategy;
import org.jooq.util.Definition;

/**
 * Makes all JOOQ POJOs extend the DomainObject interface.
 */
public class DomainObjectGeneratorStrategy
    extends DefaultGeneratorStrategy
{
    @Override
    public String getJavaClassExtends(Definition definition, Mode mode)
    {
        if ( mode == Mode.POJO)
        {
            return GeneratedDomainObject.class.getName();
        }
        return super.getJavaClassExtends(definition, mode);
    }
}
