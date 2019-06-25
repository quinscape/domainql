package de.quinscape.domainql.docs;

import java.util.Comparator;

/**
 * Sorts field docs alphabetically.
 */
public class FieldDocComparator
    implements Comparator<FieldDoc>
{
    public final static FieldDocComparator INSTANCE = new FieldDocComparator();
    
    private FieldDocComparator()
    {

    }

    @Override
    public int compare(FieldDoc o1, FieldDoc o2)
    {
        return o1.getName().compareTo(o2.getName());
    }
}
