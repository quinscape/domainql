package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.SumPerMonth;

import jakarta.validation.constraints.NotNull;

@GraphQLLogic
public class SumPerMonthLogic
{
    @GraphQLQuery
    public SumPerMonth getSumPerMonthLogic(@NotNull int sum)
    {
        final SumPerMonth sumPerMonth = new SumPerMonth();
        sumPerMonth.setYear(2019);
        sumPerMonth.setMonth(6);
        sumPerMonth.setSum(sum);
        return sumPerMonth;
    }
}
