package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.BinaryBean;

@GraphQLLogic
public class BinaryDataLogic
{
    @GraphQLQuery
    public BinaryBean binaryBean()
    {
        final BinaryBean binaryBean = new BinaryBean();
        binaryBean.setName("Henry");
        binaryBean.setData("HELLO".getBytes());
        return binaryBean;
    }
}
