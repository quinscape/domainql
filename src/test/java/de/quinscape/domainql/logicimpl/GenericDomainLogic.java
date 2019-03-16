package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.spring.jsview.util.JSONUtil;

import java.util.Map;
import java.util.TreeMap;

@GraphQLLogic
public class GenericDomainLogic
{
    @GraphQLMutation
    public String store(DomainObject domainObject)
    {

        // sort properties alphabetically
        Map<String,String> map = new TreeMap<>();

        map.put("_javaType", domainObject.getClass().getName());
        for (String name : domainObject.propertyNames())
        {
            final Object value = domainObject.getProperty(name);
            map.put(name, "=" + value + ( value != null ? " (" + value.getClass().getSimpleName() + ")" : ""));
        }
        return JSONUtil.DEFAULT_GENERATOR.forValue(map);
    }
}
