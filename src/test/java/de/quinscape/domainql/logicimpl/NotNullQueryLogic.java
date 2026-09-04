package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.annotation.GraphQLTypeParam;
import de.quinscape.domainql.beans.Payload;
import de.quinscape.domainql.util.Paged;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GraphQLLogic
public class NotNullQueryLogic
{
    private final static Logger log = LoggerFactory.getLogger(NotNullQueryLogic.class);


    @GraphQLQuery
    public <T> @NotNull Paged<T> query(
        @GraphQLTypeParam( types = { TypeA.class, TypeB.class }, typeNamePattern = "*Document") Class<T> cls,
        String name
    ) throws IllegalAccessException, InstantiationException
    {
        return new Paged<>();
    }

    @GraphQLQuery
    public <T> @NotNull Payload q2(
        String name
    )
    {
        return new Payload();
    }

    @GraphQLMutation
    public <T> @NotNull Paged<T> mutation(
        @GraphQLTypeParam( types = { TypeA.class, TypeB.class }, typeNamePattern = "*Document") Class<T> cls,
        String name
    ) throws IllegalAccessException, InstantiationException
    {
        return new Paged<>();
    }

    @GraphQLMutation
    public <T> @NotNull Payload m2(
        String name
    )
    {
        return new Payload();
    }

    public final static class TypeA
    {
        private String value;


        public String getValue()
        {
            return value;
        }


        public void setValue(String value)
        {
            this.value = value;
        }
    }

    public final static class TypeB
    {
        private String value;


        public String getValue()
        {
            return value;
        }


        public void setValue(String value)
        {
            this.value = value;
        }
    }
}
