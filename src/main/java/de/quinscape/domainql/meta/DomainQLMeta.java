package de.quinscape.domainql.meta;

import de.quinscape.domainql.GenericTypeReference;
import de.quinscape.domainql.config.RelationModel;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.svenson.JSONable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DomainQL meta data on the server-side. Basically a data map with named keys, "types" being special and containing the
 * type meta data.
 *
 * @see DomainQLTypeMeta
 */
public class DomainQLMeta
    implements JSONable
{
    private final Map<String, Object> data;

    /**
     * Name of the builtin name fields type meta
     */
    public final static String NAME_FIELDS = "nameFields";

    /**
     * Name of the builtin relations addendum
     */
    public final static String RELATIONS = "relations";

    /**
     * Name of the builtin generic types addendum
     */
    public final static String GENERIC_TYPES = "genericTypes";

    public DomainQLMeta(Map<String, DomainQLTypeMeta> types)
    {

        data = new HashMap<>();
        addAddendum("types", types);
    }


    public <T> void addAddendum(String name, T object)
    {
        final Object existing = data.get(name);
        if (existing != null)
        {
            throw new IllegalStateException("Data key '" + name + "' already set.");
        }
        data.put(name, object);
    }

    public DomainQLTypeMeta getTypeMeta(String typeName)
    {
        final Map<String, Object> typesMap = (Map<String, Object>) data.get("types");
        final DomainQLTypeMeta domainQLTypeMeta = (DomainQLTypeMeta) typesMap.get(typeName);
        if (domainQLTypeMeta == null)
        {
            throw new IllegalStateException("Invalid type '" + typeName + "'");
        }
        return domainQLTypeMeta;
    }


    public Map<String, Object> getData()
    {
        return data;
    }


    public List<RelationModel> getRelationModels()
    {
        return (List<RelationModel>) data.get(RELATIONS);
    }

    public List<GenericTypeReference> getGenericTypes()
    {
        return (List<GenericTypeReference>) data.get(GENERIC_TYPES);
    }

    @Override
    public String toJSON()
    {
        return JSONUtil.DEFAULT_GENERATOR.forValue(data);
    }
}
