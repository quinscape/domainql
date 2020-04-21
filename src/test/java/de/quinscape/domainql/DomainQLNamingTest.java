package de.quinscape.domainql;

import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.pojos.Bar;
import de.quinscape.domainql.testdomain.tables.pojos.BarOrg;
import de.quinscape.domainql.testdomain.tables.pojos.BarOwner;
import de.quinscape.domainql.testdomain.tables.pojos.Foo;
import org.junit.Test;
import org.svenson.JSON;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static de.quinscape.domainql.testdomain.Tables.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DomainQLNamingTest
{
    @Test
    public void testNamingFields()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)

            .configureRelation(BAR.OWNER_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)
            .configureRelation(BAR_OWNER.ORG_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)

            .configureNameFieldForTypes("name", BarOwner.class, BarOrg.class, Foo.class)
            .configureNameFields(Bar.class,"name", "owner.name", "owner.org.name")
            .build();


        final Map<String, List<String>> nameFields = domainQL.getNameFields();

        assertThat( nameFields.get("Bar"), is(Arrays.asList("name", "owner.name", "owner.org.name")) );
        assertThat( nameFields.get("BarOwner"), is(Collections.singletonList("name")) );
        assertThat( nameFields.get("BarOrg"), is(Collections.singletonList("name")) );
        assertThat( nameFields.get("Foo"), is(Collections.singletonList("name")) );
    }


    @Test(expected = DomainQLTypeException.class)
    public void testNamingFieldsManyToMany()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)

            .configureRelation(BAR.OWNER_ID, SourceField.OBJECT_AND_SCALAR, TargetField.MANY)
            .configureRelation(BAR_OWNER.ORG_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)

            // this makes sense from a GraphQL logic point of view, but we don't want the complications
            // and the use-case for this is weak at best
            .configureNameFields(BarOwner.class,"name", "bars.name")
            .build();


    }

    @Test(expected = DomainQLTypeException.class)
    public void testNamingFieldsError()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)

            .configureRelation(BAR.OWNER_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)
            .configureRelation(BAR_OWNER.ORG_ID, SourceField.OBJECT_AND_SCALAR, TargetField.NONE)

            .configureNameFields(Bar.class,"name", "wrong.name")
            .build();
    }
}
