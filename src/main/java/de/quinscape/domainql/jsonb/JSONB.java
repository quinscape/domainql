package de.quinscape.domainql.jsonb;

import de.quinscape.spring.jsview.util.JSONUtil;
import org.svenson.DynamicProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Container for json or a jsonb field. 
 */
public final class JSONB
    implements DynamicProperties
{
    private Map<String,Object> storage;
    private Map<String,Object> storageRO;


    public JSONB()
    {
        this(null);
    }
    
    public JSONB(Map<String, Object> storage)
    {
        this.storage = storage;
    }


    @Override
    public void setProperty(String name, Object value)
    {
        if (storage == null)
        {
            storage = new HashMap<>();
            storageRO = Collections.unmodifiableMap(storage);
        }
        storage.put(name, value);
    }


    @Override
    public Object getProperty(String name)
    {

        if (storage == null)
        {
            return Collections.emptyMap();
        }

        return storage.get(name);
    }


    @Override
    public Set<String> propertyNames()
    {
        if (storage == null)
        {
            return Collections.emptySet();
        }
        return storage.keySet();
    }


    @Override
    public boolean hasProperty(String name)
    {
        if (storage == null)
        {
            return false;
        }
        return storage.containsKey(name);
    }


    @Override
    public Object removeProperty(String name)
    {
        if (storage == null)
        {
            return null;
        }
        return storage.remove(name);
    }


    public Map<String, Object> asMap()
    {
        return storageRO;
    }

    public static JSONB forValue(String json)
    {
        if (json == null)
        {
            return null;
        }
        return JSONUtil.DEFAULT_PARSER.parse(JSONB.class, json);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o instanceof JSONB)
        {
            JSONB that = (JSONB) o;
            return Objects.equals(storage, that.storage);

        }

        return false;
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(storage);
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "storage = " + storage
            ;
    }


    public String toJSON()
    {
        return JSONUtil.DEFAULT_GENERATOR.forValue(this);
    }
}
