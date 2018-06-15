package de.quinscape.domainql.schema;

import de.quinscape.spring.jsview.JsView;
import de.quinscape.spring.jsview.JsViewContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestJsViewContext
    implements JsViewContext
{
    private Map<String, Object> viewData = new HashMap<>();

    private Map<String, Object> placeHolderValues = new HashMap<>();


    @Override
    public JsView getJsView()
    {
        return null;
    }


    @Override
    public Map<String, Object> getPlaceHolderValues()
    {
        return placeHolderValues;
    }


    @Override
    public Map<String, ?> getSpringModel()
    {
        return Collections.emptyMap();
    }


    @Override
    public HttpServletRequest getRequest()
    {
        return null;
    }


    @Override
    public Map<String, Object> getViewData()
    {
        return viewData;
    }


    @Override
    public void setPlaceholderValue(String name, Object value)
    {
         placeHolderValues.put(name, value);
    }


    @Override
    public void provideViewData(String name, Object value)
    {
        viewData.put(name, value);
    }
}
