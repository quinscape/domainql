package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.testdomain.tables.pojos.SourceOne;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GraphQLLogic
public class LogicWithMirrorInput
{
    private final static Logger log = LoggerFactory.getLogger(LogicWithMirrorInput.class);

    private final DSLContext dslContext;


    public LogicWithMirrorInput()
    {
        this(null);
    }

    public LogicWithMirrorInput(DSLContext dslContext)
    {
        this.dslContext = dslContext;
    }


    @GraphQLQuery
    public boolean queryWithMirrorInput(SourceOne inputOne)
    {
        return true;
    }

}
