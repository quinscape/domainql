package de.quinscape.domainql.mock;

import de.quinscape.domainql.testdomain.tables.records.SourceEightRecord;
import de.quinscape.domainql.testdomain.tables.records.SourceFiveRecord;
import de.quinscape.domainql.testdomain.tables.records.SourceOneRecord;
import de.quinscape.domainql.testdomain.tables.records.SourceSixRecord;
import de.quinscape.domainql.testdomain.tables.records.TargetEightRecord;
import de.quinscape.domainql.testdomain.tables.records.TargetFiveRecord;
import de.quinscape.domainql.testdomain.tables.records.TargetNineCountsRecord;
import de.quinscape.domainql.testdomain.tables.records.TargetNineRecord;
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
        else if (sql.equals("select \"public\".\"source_eight\".\"id\", \"public\".\"source_eight\".\"target_name\", " +
            "\"public\".\"source_eight\".\"target_num\" from \"public\".\"source_eight\""))
        {
            return new MockResult[] {
                new MockResult(
                    1,
                    results(
                        new SourceEightRecord("source-id", "target-name", 123)
                    )
                )
            };

        }
        else if (sql.equals("select \"public\".\"target_eight\".\"id\", \"public\".\"target_eight\".\"name\", " +
            "\"public\".\"target_eight\".\"num\" from \"public\".\"target_eight\" where (\"public\".\"target_eight\"" +
            ".\"name\" = ? and \"public\".\"target_eight\".\"num\" = ?)"))
        {
            return new MockResult[] {
                new MockResult(
                    1,
                    results(
                        new TargetEightRecord("target-id", "target-name", 123)
                    )
                )
            };

        }
        else if (sql.equals("select \"public\".\"target_eight\".\"id\", \"public\".\"target_eight\".\"name\", " +
            "\"public\".\"target_eight\".\"num\" from \"public\".\"target_eight\""))
        {
            return new MockResult[] {
                new MockResult(
                    1,
                    results(
                        new TargetEightRecord("target-id", "target-name", 345)
                    )
                )
            };

        }
        else if (sql.equals("select \"public\".\"source_eight\".\"id\", \"public\".\"source_eight\".\"target_name\", " +
            "\"public\".\"source_eight\".\"target_num\" from \"public\".\"source_eight\" where (\"public\"" +
            ".\"source_eight\".\"target_name\" = ? and \"public\".\"source_eight\".\"target_num\" = ?)"))
        {
            return new MockResult[] {
                new MockResult(
                    2,
                    results(
                        new SourceEightRecord("target-id", "target-name", 456),
                        new SourceEightRecord("target-id-2", "target-name-2", 567)
                    )
                )
            };

        }
        else if (sql.equals("select \"public\".\"target_nine_counts\".\"target_id\", \"public\"" +
            ".\"target_nine_counts\".\"count\" from \"public\".\"target_nine_counts\""))
        {
            return new MockResult[] {
                new MockResult(
                    1,
                    results(
                        new TargetNineCountsRecord("target-id", 123L)
                    )
                )
            };

        }
        else if (sql.equals("select \"public\".\"target_nine\".\"id\", \"public\".\"target_nine\".\"name\" from " +
            "\"public\".\"target_nine\" where \"public\".\"target_nine\".\"id\" = ?"))
        {
            return new MockResult[] {
                new MockResult(
                    1,
                    results(
                        new TargetNineRecord("target-id", "target 9 name")
                    )
                )
            };

        }
        else if (sql.equals("select \"public\".\"target_nine\".\"id\", \"public\".\"target_nine\".\"name\" from " +
            "\"public\".\"target_nine\""))
        {
            return new MockResult[] {
                new MockResult(
                    1,
                    results(
                        new TargetNineRecord("target-id", "target 9 name")
                    )
                )
            };

        }
        else if (sql.equals("select \"public\".\"target_nine_counts\".\"target_id\", \"public\"" +
            ".\"target_nine_counts\".\"count\" from \"public\".\"target_nine_counts\" where \"public\"" +
            ".\"target_nine_counts\".\"target_id\" = ?"))
        {
            return new MockResult[] {
                new MockResult(
                    1,
                    results(
                        new TargetNineCountsRecord("target-id", 234L)
                    )
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


