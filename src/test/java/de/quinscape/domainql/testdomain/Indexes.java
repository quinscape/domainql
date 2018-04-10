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

import javax.annotation.Generated;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.Internal;


/**
 * A class modelling indexes of tables of the <code>public</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.6"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index PK_SOURCE_FIVE = Indexes0.PK_SOURCE_FIVE;
    public static final Index PK_SOURCE_FOUR = Indexes0.PK_SOURCE_FOUR;
    public static final Index PK_SOURCE_ONE = Indexes0.PK_SOURCE_ONE;
    public static final Index PK_SOURCE_SIX = Indexes0.PK_SOURCE_SIX;
    public static final Index PK_SOURCE_THREE = Indexes0.PK_SOURCE_THREE;
    public static final Index PK_SOURCE_TWO = Indexes0.PK_SOURCE_TWO;
    public static final Index PK_TARGET_FIVE = Indexes0.PK_TARGET_FIVE;
    public static final Index PK_TARGET_FOUR = Indexes0.PK_TARGET_FOUR;
    public static final Index PK_TARGET_ONE = Indexes0.PK_TARGET_ONE;
    public static final Index PK_TARGET_SIX = Indexes0.PK_TARGET_SIX;
    public static final Index PK_TARGET_THREE = Indexes0.PK_TARGET_THREE;
    public static final Index PK_TARGET_TWO = Indexes0.PK_TARGET_TWO;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 {
        public static Index PK_SOURCE_FIVE = Internal.createIndex("pk_source_five", SourceFive.SOURCE_FIVE, new OrderField[] { SourceFive.SOURCE_FIVE.ID }, true);
        public static Index PK_SOURCE_FOUR = Internal.createIndex("pk_source_four", SourceFour.SOURCE_FOUR, new OrderField[] { SourceFour.SOURCE_FOUR.ID }, true);
        public static Index PK_SOURCE_ONE = Internal.createIndex("pk_source_one", SourceOne.SOURCE_ONE, new OrderField[] { SourceOne.SOURCE_ONE.ID }, true);
        public static Index PK_SOURCE_SIX = Internal.createIndex("pk_source_six", SourceSix.SOURCE_SIX, new OrderField[] { SourceSix.SOURCE_SIX.ID }, true);
        public static Index PK_SOURCE_THREE = Internal.createIndex("pk_source_three", SourceThree.SOURCE_THREE, new OrderField[] { SourceThree.SOURCE_THREE.ID }, true);
        public static Index PK_SOURCE_TWO = Internal.createIndex("pk_source_two", SourceTwo.SOURCE_TWO, new OrderField[] { SourceTwo.SOURCE_TWO.ID }, true);
        public static Index PK_TARGET_FIVE = Internal.createIndex("pk_target_five", TargetFive.TARGET_FIVE, new OrderField[] { TargetFive.TARGET_FIVE.ID }, true);
        public static Index PK_TARGET_FOUR = Internal.createIndex("pk_target_four", TargetFour.TARGET_FOUR, new OrderField[] { TargetFour.TARGET_FOUR.ID }, true);
        public static Index PK_TARGET_ONE = Internal.createIndex("pk_target_one", TargetOne.TARGET_ONE, new OrderField[] { TargetOne.TARGET_ONE.ID }, true);
        public static Index PK_TARGET_SIX = Internal.createIndex("pk_target_six", TargetSix.TARGET_SIX, new OrderField[] { TargetSix.TARGET_SIX.ID }, true);
        public static Index PK_TARGET_THREE = Internal.createIndex("pk_target_three", TargetThree.TARGET_THREE, new OrderField[] { TargetThree.TARGET_THREE.ID }, true);
        public static Index PK_TARGET_TWO = Internal.createIndex("pk_target_two", TargetTwo.TARGET_TWO, new OrderField[] { TargetTwo.TARGET_TWO.ID }, true);
    }
}