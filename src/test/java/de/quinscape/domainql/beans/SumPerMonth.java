package de.quinscape.domainql.beans;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Table(name = "sum_per_month")
public class SumPerMonth
{
    private int month;
    private int year;
    private int sum;


    @Column(name = "month")
    @NotNull
    public int getMonth()
    {
        return month;
    }


    public void setMonth(int month)
    {
        this.month = month;
    }


    @Column(name = "year")
    @NotNull
    public int getYear()
    {
        return year;
    }


    public void setYear(int year)
    {
        this.year = year;
    }


    @Column(name = "sum")
    public int getSum()
    {
        return sum;
    }


    public void setSum(int sum)
    {
        this.sum = sum;
    }
}
