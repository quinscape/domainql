package de.quinscape.domainql.jsonb;

import de.quinscape.spring.jsview.util.JSONUtil;
import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Objects;

/**
 * Postgresql jsonb binding for JOOQ
 */
public final class PGJSONBinding
    implements Binding<Object, JSONB>
{

    // The converter does all the work
    @Override
    public Converter<Object, JSONB> converter()
    {
        return new Converter<Object, JSONB>()
        {
            @Override
            public JSONB from(Object t)
            {
                return JSONB.forValue((String) t);
            }


            @Override
            public Object to(JSONB u)
            {
                return u.toJSON();
            }


            @Override
            public Class<Object> fromType()
            {
                return Object.class;
            }


            @Override
            public Class<JSONB> toType()
            {
                return JSONB.class;
            }
        };
    }


    // Rending a bind variable for the binding context's value and casting it to the json type
    @Override
    public void sql(BindingSQLContext<JSONB> ctx) throws SQLException
    {
        // Depending on how you generate your SQL, you may need to explicitly distinguish
        // between jOOQ generating bind variables or inlined literals.
        if (ctx.render().paramType() == ParamType.INLINED)
        {
            ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("::json");
        }
        else
        {
            ctx.render().sql("?::json");
        }
    }


    // Registering VARCHAR types for JDBC CallableStatement OUT parameters
    @Override
    public void register(BindingRegisterContext<JSONB> ctx) throws SQLException
    {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }


    // Converting the JSONB to a String value and setting that on a JDBC PreparedStatement
    @Override
    public void set(BindingSetStatementContext<JSONB> ctx) throws SQLException
    {
        ctx.statement().setString(ctx.index(), Objects.toString(ctx.convert(converter()).value(), null));
    }


    // Getting a String value from a JDBC ResultSet and converting that to a JSONB
    @Override
    public void get(BindingGetResultSetContext<JSONB> ctx) throws SQLException
    {
        ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
    }


    // Getting a String value from a JDBC CallableStatement and converting that to a JSONB
    @Override
    public void get(BindingGetStatementContext<JSONB> ctx) throws SQLException
    {
        ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));
    }


    // Setting a value on a JDBC SQLOutput (useful for Oracle OBJECT types)
    @Override
    public void set(BindingSetSQLOutputContext<JSONB> ctx) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }


    // Getting a value from a JDBC SQLInput (useful for Oracle OBJECT types)
    @Override
    public void get(BindingGetSQLInputContext<JSONB> ctx) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }
}
