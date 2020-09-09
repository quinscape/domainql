package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.BDContainer;
import de.quinscape.domainql.beans.BIContainer;

import java.math.BigDecimal;
import java.math.BigInteger;

@GraphQLLogic
public class BigNumericLogic
{
    @GraphQLQuery
    public BDContainer bigDecimalQuery(BDContainer in)
    {
        in.setValue(
            in.getValue().add(
                new BigDecimal("1.23")
            )
        );
        return in;
    }


    @GraphQLQuery
    public BIContainer bigIntegerQuery(BIContainer in)
    {
        in.setValue(
            in.getValue().add(
                new BigInteger("1")
            )
        );

        return in;
    }
}
