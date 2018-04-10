/*
 * This file is generated by jOOQ.
*/
package de.quinscape.domainql.testdomain;


import de.quinscape.domainql.testdomain.tables.SourceFive;
import de.quinscape.domainql.testdomain.tables.SourceFour;
import de.quinscape.domainql.testdomain.tables.SourceOne;
import de.quinscape.domainql.testdomain.tables.SourceSix;
import de.quinscape.domainql.testdomain.tables.SourceThree;
import de.quinscape.domainql.testdomain.tables.SourceTwo;
import de.quinscape.domainql.testdomain.tables.TargetFive;
import de.quinscape.domainql.testdomain.tables.TargetFour;
import de.quinscape.domainql.testdomain.tables.TargetOne;
import de.quinscape.domainql.testdomain.tables.TargetSix;
import de.quinscape.domainql.testdomain.tables.TargetThree;
import de.quinscape.domainql.testdomain.tables.TargetTwo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.6"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = -1207658107;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.source_five</code>.
     */
    public final SourceFive SOURCE_FIVE = de.quinscape.domainql.testdomain.tables.SourceFive.SOURCE_FIVE;

    /**
     * The table <code>public.source_four</code>.
     */
    public final SourceFour SOURCE_FOUR = de.quinscape.domainql.testdomain.tables.SourceFour.SOURCE_FOUR;

    /**
     * The table <code>public.source_one</code>.
     */
    public final SourceOne SOURCE_ONE = de.quinscape.domainql.testdomain.tables.SourceOne.SOURCE_ONE;

    /**
     * The table <code>public.source_six</code>.
     */
    public final SourceSix SOURCE_SIX = de.quinscape.domainql.testdomain.tables.SourceSix.SOURCE_SIX;

    /**
     * The table <code>public.source_three</code>.
     */
    public final SourceThree SOURCE_THREE = de.quinscape.domainql.testdomain.tables.SourceThree.SOURCE_THREE;

    /**
     * The table <code>public.source_two</code>.
     */
    public final SourceTwo SOURCE_TWO = de.quinscape.domainql.testdomain.tables.SourceTwo.SOURCE_TWO;

    /**
     * The table <code>public.target_five</code>.
     */
    public final TargetFive TARGET_FIVE = de.quinscape.domainql.testdomain.tables.TargetFive.TARGET_FIVE;

    /**
     * The table <code>public.target_four</code>.
     */
    public final TargetFour TARGET_FOUR = de.quinscape.domainql.testdomain.tables.TargetFour.TARGET_FOUR;

    /**
     * The table <code>public.target_one</code>.
     */
    public final TargetOne TARGET_ONE = de.quinscape.domainql.testdomain.tables.TargetOne.TARGET_ONE;

    /**
     * The table <code>public.target_six</code>.
     */
    public final TargetSix TARGET_SIX = de.quinscape.domainql.testdomain.tables.TargetSix.TARGET_SIX;

    /**
     * The table <code>public.target_three</code>.
     */
    public final TargetThree TARGET_THREE = de.quinscape.domainql.testdomain.tables.TargetThree.TARGET_THREE;

    /**
     * The table <code>public.target_two</code>.
     */
    public final TargetTwo TARGET_TWO = de.quinscape.domainql.testdomain.tables.TargetTwo.TARGET_TWO;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            SourceFive.SOURCE_FIVE,
            SourceFour.SOURCE_FOUR,
            SourceOne.SOURCE_ONE,
            SourceSix.SOURCE_SIX,
            SourceThree.SOURCE_THREE,
            SourceTwo.SOURCE_TWO,
            TargetFive.TARGET_FIVE,
            TargetFour.TARGET_FOUR,
            TargetOne.TARGET_ONE,
            TargetSix.TARGET_SIX,
            TargetThree.TARGET_THREE,
            TargetTwo.TARGET_TWO);
    }
}