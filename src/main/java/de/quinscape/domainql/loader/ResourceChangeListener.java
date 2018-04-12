package de.quinscape.domainql.loader;


public interface ResourceChangeListener
{
    void onResourceChange(ResourceEvent resourceEvent, String rootPath, String resourcePath);
}
