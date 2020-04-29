package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.testdomain.tables.pojos.Bar;

@GraphQLLogic
public class NullPropInDomainObjectLogic
{
    @GraphQLQuery
    public DomainObject fetch(DomainObject in, String name)
    {
        final Bar foo = new Bar();

        foo.setId("38fb6d2b-4946-4b96-8912-bfe81cce2fc0");
        foo.setName(name);
        foo.setOwnerId("3ef7126b-ac62-4cb9-a01c-684eaeeb6b3a");

        // this is the point of this test. A null prop inside a DomainObject
        foo.setDescription(null);
        return foo;
    }
}
