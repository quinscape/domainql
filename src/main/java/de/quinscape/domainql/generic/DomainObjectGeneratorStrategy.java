package de.quinscape.domainql.generic;

import org.jooq.util.DefaultGeneratorStrategy;
import org.jooq.util.Definition;

import java.util.Collections;
import java.util.List;

/**
 * Makes all JOOQ POJOs extend the DomainObject interface.
 */
public class DomainObjectGeneratorStrategy
    extends DefaultGeneratorStrategy
{
    @Override
    public List<String> getJavaClassImplements(
        Definition definition, Mode mode
    )
    {
        if ( mode == Mode.POJO)
        {
            return Collections.singletonList(DomainObject.class.getName());
        }
        return super.getJavaClassImplements(definition, mode);
    }
}
