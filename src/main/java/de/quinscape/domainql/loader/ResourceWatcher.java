package de.quinscape.domainql.loader;

public interface ResourceWatcher
{
    void register(ResourceChangeListener listener);

    void clearListeners();

    void enable();

    void disable();
}
