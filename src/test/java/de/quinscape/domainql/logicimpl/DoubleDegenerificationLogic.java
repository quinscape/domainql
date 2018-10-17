package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.beans.Container;
import de.quinscape.domainql.beans.ContainerProp;
import de.quinscape.domainql.beans.Payload;

/**
 * Tests a generic container embedding another generic container directly as prop.
 * (<code>Container&lt;T&gt;</code>, not <code>T</code> or <code>List&lt;T&gt</code>
 */
@GraphQLLogic
public class DoubleDegenerificationLogic
{
    @GraphQLMutation
    public String mutationWithDD(ContainerProp<Payload> c)
    {
        final Container<Payload> container = c.getValue();
        final Payload payload = container.getValue();
        return "[" + payload.getName() + ":" + payload.getNum() + ":" + container.getNum() + "]";
    }
}
