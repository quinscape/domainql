package de.quinscape.domainql.mock;

import de.quinscape.domainql.testdomain.tables.records.SourceFiveRecord;
import de.quinscape.domainql.testdomain.tables.records.SourceOneRecord;
import de.quinscape.domainql.testdomain.tables.records.SourceSixRecord;
import de.quinscape.domainql.testdomain.tables.records.TargetFiveRecord;
import de.quinscape.domainql.testdomain.tables.records.TargetSixRecord;
import de.quinscape.domainql.testdomain.tables.records.TargetThreeRecord;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static org.jooq.impl.DSL.*;

public class TestProvider
    implements MockDataProvider
{
    private final static Logger log = LoggerFactory.getLogger(TestProvider.class);


    @Override
    public MockResult[] execute(MockExecuteContext ctx) throws SQLException
    {
        final String sql = ctx.sql();
        if (sql.equals("select \"public\".\"source_three\".\"id\", \"public\".\"source_three\".\"target_id\" from \"public\".\"source_three\""))
        {
            return new MockResult[] {
                new MockResult(
                    new SourceOneRecord("id1", "id2")
                )
            };
        }
        else if (sql.equals("select \"public\".\"target_three\".\"id\" from \"public\".\"target_three\" where \"public\".\"target_three\".\"id\" = ?"))
        {
            return new MockResult[] {
                new MockResult(
                    new TargetThreeRecord("id2")
                )
            };
        }


        else if (sql.equals("select \"public\".\"target_six\".\"id\" from \"public\".\"target_six\""))
        {
            return new MockResult[] {
                new MockResult(
                    new TargetSixRecord("target-id")
                )
            };
        }
        else if (sql.equals("select \"public\".\"source_six\".\"id\", \"public\".\"source_six\".\"target_id\" from \"public\".\"source_six\" where \"public\".\"source_six\".\"target_id\" = ?"))
        {
            return new MockResult[] {
                new MockResult(
                    2,
                    results(
                        new SourceSixRecord("source-id", "target-id"),
                        new SourceSixRecord("source-id2", "target-id")
                    )
                )
            };
        }

        else if (sql.equals("select \"public\".\"target_five\".\"id\" from \"public\".\"target_five\""))
        {
            return new MockResult[] {
                new MockResult(
                    new TargetFiveRecord("target-id")
                )
            };
        }
        else if (sql.equals("select \"public\".\"source_five\".\"id\", \"public\".\"source_five\".\"target_id\" from " +
            "\"public\".\"source_five\" where \"public\".\"source_five\".\"target_id\" = ?"))
        {
            return new MockResult[] {
                new MockResult(
                    new SourceFiveRecord("src-id", "target-id")
                )
            };
        }

        throw new IllegalStateException("Unhandled SQL statement: " + sql);
    }

    static Result<?> results(Record... data) {
        Result<Record> result = using(data[0].configuration()).newResult(data[0].fields());
        for (Record datum : data)
        {
            result.add(datum);
        }

        return result;
    }

}


