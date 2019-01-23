package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.annotation.GraphQLTypeParam;
import de.quinscape.domainql.beans.ComplexInput;
import de.quinscape.domainql.beans.Container;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@GraphQLLogic
public class TypeParamLogic
{
    private final static Logger log = LoggerFactory.getLogger(TypeParamLogic.class);


    @GraphQLQuery
    public <T> T query(
        @GraphQLTypeParam(types = { TypeA.class, TypeB.class }) Class<T> cls,
        ComplexInput complexInput
    ) throws IllegalAccessException, InstantiationException
    {

        final T bean = cls.newInstance();

        JSONUtil.DEFAULT_UTIL.setProperty(bean,"value", complexInput.getValue() + "/" + complexInput.getNum());


        return bean;
    }


    @GraphQLQuery
    public <T> Container<T> queryContainer(
        @GraphQLTypeParam(types = { TypeA.class, TypeB.class }) Class<T> cls,
        ComplexInput complexInput
    ) throws IllegalAccessException, InstantiationException
    {

        final T bean = cls.newInstance();

        JSONUtil.DEFAULT_UTIL.setProperty(bean,"value", complexInput.getValue() + "/" + complexInput.getNum());


        final Container<T> container = new Container<>();
        container.setValue(bean);
        container.setNum(123);
        return container;
    }


    @GraphQLQuery
    public <T> List<T> queryList(
        @GraphQLTypeParam(types = { TypeA.class, TypeB.class }) Class<T> cls,
        ComplexInput complexInput
    ) throws IllegalAccessException, InstantiationException
    {

        final ArrayList<T> list = new ArrayList<>();

        final T bean = cls.newInstance();
        JSONUtil.DEFAULT_UTIL.setProperty(bean,"value", complexInput.getValue() + "...");
        list.add(bean);

        final T bean2 = cls.newInstance();
        JSONUtil.DEFAULT_UTIL.setProperty(bean2,"value", "..." + complexInput.getNum());
        list.add(bean2);

        return list;
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
