package de.quinscape.domainql.meta;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.OutputType;
import de.quinscape.domainql.meta.DomainQLMeta;
import de.quinscape.domainql.meta.MetadataProvider;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adapter that write the internal name field configuration into the DomainQL meta data.
 */
public class NameFieldProvider
    implements MetadataProvider
{

    private final Map<String, List<String>> nameFields;

    private final Set<String> nameFieldsByName;


    public NameFieldProvider(Map<String, List<String>> nameFields, Set<String> nameFieldsByName)
    {
        this.nameFields = nameFields;
        this.nameFieldsByName = nameFieldsByName;
    }


    @Override
    public void provideMetaData(DomainQL domainQL, DomainQLMeta meta)
    {

        final GraphQLSchema schema = domainQL.getGraphQLSchema();

        for (GraphQLNamedType value : schema.getTypeMap().values())
        {
            final String typeName = value.getName();
            if (value instanceof GraphQLObjectType && !typeName.startsWith("_"))
            {

                for (GraphQLFieldDefinition fieldDef : ((GraphQLObjectType) value).getFieldDefinitions())
                {
                    final String name = fieldDef.getName();
                    if (nameFieldsByName.contains(name))
                    {
                        if (!nameFields.containsKey(typeName))
                        {
                            final OutputType outputType = domainQL.getTypeRegistry().lookup(typeName);
                            if (outputType == null)
                            {
                                throw new IllegalStateException("Could find find type '" + typeName + "'");
                            }

                            meta.getTypeMeta(outputType.getName()).setMeta(
                                "nameFields",
                                Collections.singletonList(name)
                            );
                        }
                        break;
                    }
                }
            }
        }

        for (Map.Entry<String, List<String>> e : nameFields.entrySet())
        {
            meta.getTypeMeta(e.getKey()).setMeta(DomainQLMeta.NAME_FIELDS, e.getValue());
        }

    }
}
