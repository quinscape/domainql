package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.scalar.GraphQLTimestampScalar;
import de.quinscape.domainql.testdomain.tables.pojos.Foo;

import java.sql.Timestamp;

@GraphQLLogic
public class GenericDomainOutputLogic
{
    @GraphQLQuery
    public DomainObject queryDomainObject()
    {
        final Foo foo = new Foo();
        foo.setId("c5a27eec-ab1e-4b66-8af1-544e291eda36");
        foo.setName("FooAsDomainObj");
        foo.setNum(9876);
        foo.setCreated(GraphQLTimestampScalar.convert("2018-01-01T12:34:56.123Z"));
        return foo;
    }
}
