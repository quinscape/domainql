package de.quinscape.domainql.loader;

import de.quinscape.domainql.DomainQLException;
import de.quinscape.domainql.util.JSONUtil;
import org.svenson.tokenize.InputStreamSource;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Converter that parses JSON into a target type and back with svenson.
 *
 * @param <T> target type
 */
public class JSONResourceConverter<T>
    implements ResourceConverter<T>
{
    private final Class<T> cls;


    public JSONResourceConverter(Class<T> cls)
    {
        this.cls = cls;
    }

    @Override
    public T readStream(InputStream data)
    {
        return JSONUtil.DEFAULT_PARSER.parse(
            cls,
            new InputStreamSource(
                data,
                true
            )
        );
    }


    @Override
    public byte[] toByteArray(T value)
    {
        try
        {
            return JSONUtil.DEFAULT_GENERATOR.forValue(value).getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new DomainQLException(e);
        }
    }
}
