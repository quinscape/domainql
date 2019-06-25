package de.quinscape.domainql.docs;

import java.util.Comparator;

/**
 * Sorts a TypeDocs by name. "QueryType" and "MutationType" first, then the rest of the types in alphabetical order.
 */
public class TypeDocComparator
    implements Comparator<TypeDoc>
{
    public final static TypeDocComparator INSTANCE = new TypeDocComparator();


    private TypeDocComparator()
    {
    }


    @Override
    public int compare(TypeDoc docA, TypeDoc docB)
    {
        final String nameA = replace(docA.getName());
        final String nameB = replace(docB.getName());
        return nameA.compareTo(nameB);
    }


    private String replace(String name)
    {
        if (name.equals(TypeDoc.QUERY_TYPE))
        {
            return "@0";
        }
        else if (name.equals(TypeDoc.MUTATION_TYPE))
        {
            return "@1";
        }
        return name;
    }
}
