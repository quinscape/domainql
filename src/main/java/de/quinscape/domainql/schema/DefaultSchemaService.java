package de.quinscape.domainql.schema;

import de.quinscape.domainql.model.Domain;
import de.quinscape.domainql.model.DomainType;
import de.quinscape.domainql.model.ForeignKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

/**
 * Default implementation of the schema service that contains the default update algorithm and delegates the actual
 * operations to a {@link DDLOperations} instance.
 */
public class DefaultSchemaService
    implements SchemaService
{
    private final static Logger log = LoggerFactory.getLogger(DefaultSchemaService.class);


    private final NamingStrategy namingStrategy;

    private final DDLOperationsFactory opFactory;


    public DefaultSchemaService(NamingStrategy namingStrategy, DDLOperationsFactory opFactory)
    {
        this.namingStrategy = namingStrategy;
        this.opFactory = opFactory;
    }


    @Override
    public void synchronizeSchema(RuntimeContext runtimeContext, DataSource dataSource)
    {
        final Domain domain = runtimeContext.getDomain();

        final String schema = runtimeContext.getSchemaName();
        final List<DomainType> domainTypes = domain.getDomainTypes();

        log.info("Synchronize schema: {}, {}", dataSource, domainTypes);



        final DDLOperations ops = opFactory.create(runtimeContext, dataSource);
        try
        {
            List<String> schemata = ops.listSchemata(runtimeContext);
            if (!schemata.contains(schema))
            {
                ops.createSchema(runtimeContext, schema);
            }

            List<String> tables = ops.listTables(runtimeContext, schema);

            for (DomainType type : domainTypes)
            {
                String tableName = namingStrategy.getTableName(type.getName());

                if (tables.contains(tableName))
                {
                    ops.dropKeys(runtimeContext, type);
                }
            }

            for (DomainType type : domainTypes)
            {

                String tableName = namingStrategy.getTableName(type.getName());
                if (tables.contains(tableName))
                {
                    ops.updateTable(runtimeContext, type);
                }
                else
                {
                    ops.createTable(runtimeContext, type);
                }
            }

            for (DomainType type : domainTypes)
            {
                ops.createPrimaryKey(runtimeContext, type);
            }

            for (DomainType type : domainTypes)
            {
                for (ForeignKey foreignKey : type.getForeignKeys())
                {
                    ops.createForeignKey(runtimeContext, type, foreignKey);
                }
            }
        }
        finally
        {
            ops.destroy();
        }

    }

    @Override
    public void removeSchema(RuntimeContext runtimeContext, DataSource dataSource, String schema)
    {
        final DDLOperations op = opFactory.create(runtimeContext, dataSource);

        op.dropSchema(runtimeContext, schema);
    }
}

