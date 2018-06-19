package de.quinscape.domainql.schema;

import de.quinscape.spring.jsview.JsViewContext;
import de.quinscape.spring.jsview.JsViewProvider;

/**
 * Js view data provider that provides the current servlet context path.
 */
public final class ContextPathProvider
    implements JsViewProvider
{
    private static final String DEFAULT_NAME = "contextPath";

    private final String viewDataName;


    /**
     * Creates a context path provider that provides the context path as {@link #DEFAULT_NAME} = "contextPath.
     */
    public ContextPathProvider()
    {
        this(DEFAULT_NAME);
    }

    /**
     * Creates a context path provider that provides the context path as the given name.
     */
    public ContextPathProvider(String viewDataName)
    {
        this.viewDataName = viewDataName;
    }


    @Override
    public void provide(JsViewContext context) throws Exception
    {
        context.provideViewData(
            viewDataName,
            context.getRequest().getContextPath()
        );
    }
}
