package de.quinscape.domainql.meta;

import org.svenson.JSONProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the additional meta data and field meta data for a given GraphQL object type.
 *
 */
public class DomainQLTypeMeta
{

    private final String name;

    private final Map<String, Object> meta = new HashMap<>();
    private final Map<String, Object> metaRO = Collections.unmodifiableMap(meta);
    private final Map<String, Map<String,Object>> fields = new HashMap<>();
    private final Map<String, Map<String,Object>> fieldsRO = Collections.unmodifiableMap(fields);

    public DomainQLTypeMeta(String name)
    {
        this.name = name;
    }


    /**
     * Sets the given as type meta data.
     *
     * @param meta          name of property
     * @param value         value
     * @param <T>           type of value
     */
    public <T> void setMeta(String meta, T value)
    {
        this.meta.put(meta, value);
    }


    /**
     * Returns the type meta data property with the given name
     *
     * @param meta          name of property
     * @param <T>           type of value
     *           
     * @return value
     */
    public <T> T getMeta(String meta)
    {
        return (T) this.meta.get(meta);
    }


    /**
     *
     * Sets the given value as a field meta data property.
     *
     * @param fieldName     field name
     * @param meta          name of property
     * @param value         value
     * @param <T>           type of value
     *
     */
    public <T> void setFieldMeta(String fieldName, String meta, T value)
    {
        Map<String, Object> map = fields.computeIfAbsent(fieldName, k -> new HashMap<>());
        map.put(meta, value);
    }


    /**
     * Returns the value of a field meta data property.
     *
     * @param fieldName     field name
     * @param meta          name of property
     * @param <T>           type of value
     *
     * @return value
     */
    public <T> T getFieldMeta(String fieldName, String meta)
    {
        final Map<String, Object> map = fields.get(fieldName);
        if (map == null)
        {
            return null;
        }
        return (T) map.get(meta);
    }


    /**
     * Returns the GraphQL type name.
     *
     * @return GraphQL type name
     */
    public String getName()
    {
        return name;
    }


    /**
     * Returns the field meta data for this type.
     */
    @JSONProperty(ignoreIfNull = true)
    public Map<String, Map<String, Object>> getFields()
    {
        if (fieldsRO.size() == 0)
        {
            return null;
        }

        return fieldsRO;
    }


    /**
     * Returns the meta data for this type.
     */
    @JSONProperty(ignoreIfNull = true)
    public Map<String, Object> getMeta()
    {
        if (metaRO.size() == 0)
        {
            return null;
        }
        return metaRO;
    }
}
