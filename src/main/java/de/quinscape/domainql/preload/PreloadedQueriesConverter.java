package de.quinscape.domainql.preload;

import de.quinscape.domainql.DomainQLException;
import de.quinscape.spring.jsview.loader.ResourceConverter;
import de.quinscape.spring.jsview.util.JSONUtil;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.tokenize.InputStreamSource;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses Nashorn to evaluate the little initial query export snippets our webpack plugin grabbed for us and put
 * into JSON.
 */
public class PreloadedQueriesConverter
    implements ResourceConverter<PreloadedQueries>
{
    private final static Logger log = LoggerFactory.getLogger(PreloadedQueriesConverter.class);

    private NashornScriptEngine engine = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("nashorn");

    @Override
    public PreloadedQueries readStream(InputStream inputStream)
    {

        Map<String, String> entryPointToSource = JSONUtil.DEFAULT_PARSER.parse(
            Map.class,
            new InputStreamSource(
                inputStream,
                true
            )
        );

        final Map<String, List<PreloadedQuery>> queryMap = new HashMap<>();

        for (Map.Entry<String, String> e : entryPointToSource.entrySet())
        {
            final String entryPointName = e.getKey();
            final String querySource = e.getValue();

            final List<PreloadedQuery> initialQueries = processExportSource(
                engine,
                querySource
            );

            queryMap.put(entryPointName, initialQueries);
        }
        return new PreloadedQueries(queryMap);
    }


    @Override
    public byte[] toByteArray(PreloadedQueries value)
    {
        // We don't ever write back to the JSON file
        throw new UnsupportedOperationException();
    }

    private static List<PreloadedQuery> processExportSource(
        NashornScriptEngine engine,
        String querySource
    )
    {
        log.debug("querySource = {}", querySource);

        try
        {
            engine.eval(
                "var PRELOADED_QUERIES = " + querySource + "; PRELOADED_QUERIES = JSON.stringify(PRELOADED_QUERIES)");
            final String eval = (String) engine.getContext().getAttribute("PRELOADED_QUERIES");

            final Object o = JSONUtil.DEFAULT_PARSER.parse(eval);

            Map<String,Map> multiMap = (Map<String, Map>) o;

            List<PreloadedQuery> list = new ArrayList<>();
            for (Map.Entry<String, Map> e : multiMap.entrySet())
            {
                final PreloadedQuery preloadedQuery = prepareQueryDefinition(e.getKey(), e.getValue());
                list.add(preloadedQuery);
            }
            return list;
        }
        catch (ScriptException e1)
        {
            throw new DomainQLException(e1);
        }
    }


    private static PreloadedQuery prepareQueryDefinition(String name, Object o)
    {
        Map<String, Object> query;

        if (o instanceof String)
        {
            query = new HashMap<>();
            query.put("query", (String) o);
        }
        else if (o instanceof Map)
        {
            query = (Map<String, Object>) o;
        }
        else
        {
            throw new IllegalStateException("Invalid query object: " + o);
        }

        return new PreloadedQuery(name, query);
    }
}
