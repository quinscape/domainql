package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.annotation.GraphQLTypeParam;
import de.quinscape.domainql.beans.ComplexInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GraphQLLogic
public class TypeParamWithNamePatternLogic
{
    private final static Logger log = LoggerFactory.getLogger(TypeParamWithNamePatternLogic.class);


    @GraphQLQuery
    public <T> T query(
        @GraphQLTypeParam( types = { TypeA.class, TypeB.class }, namePattern = "*Query") Class<T> cls,
        ComplexInput complexInput
    ) throws IllegalAccessException, InstantiationException
    {
        return null;
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
