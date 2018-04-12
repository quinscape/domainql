package de.quinscape.domainql.util;


import de.quinscape.domainql.DomainQLException;
import de.quinscape.domainql.model.ConfigValue;
import de.quinscape.domainql.model.DomainField;
import org.apache.commons.beanutils.ConvertUtils;

import java.util.List;

public final class ConfigUtil
{
    private ConfigUtil()
    {

    }


    public static <T> T from(DomainField domainField, String name, T defaultValue)
    {
        return get(domainField.getConfig(), name, defaultValue);
    }


    public static <T> T get(List<ConfigValue> config, String name, T defaultValue)
    {
        if (defaultValue == null)
        {
            throw new IllegalArgumentException("defaultValue can't be null");
        }

        for (ConfigValue val : config)
        {
            if (val.getName().equals(name))
            {
                try
                {
                    final String value = val.getValue();
                    if (value != null && !defaultValue.getClass().isAssignableFrom(value.getClass()))
                    {
                        return (T) ConvertUtils.convert(value, defaultValue.getClass());
                    }
                    return (T) value;
                }
                catch (ClassCastException e)
                {
                    throw new DomainQLException("Config value '" + name + "' is not of expected type " + defaultValue.getClass().getName(), e);
                }
            }
        }
        return defaultValue;
    }
}
