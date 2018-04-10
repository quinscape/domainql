/*
 * This file is generated by jOOQ.
*/
package de.quinscape.domainql.testdomain.tables.daos;


import de.quinscape.domainql.testdomain.tables.TargetFive;
import de.quinscape.domainql.testdomain.tables.records.TargetFiveRecord;

import java.util.List;

import javax.annotation.Generated;

import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


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
@Repository
public class TargetFiveDao extends DAOImpl<TargetFiveRecord, de.quinscape.domainql.testdomain.tables.pojos.TargetFive, String> {

    /**
     * Create a new TargetFiveDao without any configuration
     */
    public TargetFiveDao() {
        super(TargetFive.TARGET_FIVE, de.quinscape.domainql.testdomain.tables.pojos.TargetFive.class);
    }

    /**
     * Create a new TargetFiveDao with an attached configuration
     */
    @Autowired
    public TargetFiveDao(Configuration configuration) {
        super(TargetFive.TARGET_FIVE, de.quinscape.domainql.testdomain.tables.pojos.TargetFive.class, configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getId(de.quinscape.domainql.testdomain.tables.pojos.TargetFive object) {
        return object.getId();
    }

    /**
     * Fetch records that have <code>id IN (values)</code>
     */
    public List<de.quinscape.domainql.testdomain.tables.pojos.TargetFive> fetchById(String... values) {
        return fetch(TargetFive.TARGET_FIVE.ID, values);
    }

    /**
     * Fetch a unique record that has <code>id = value</code>
     */
    public de.quinscape.domainql.testdomain.tables.pojos.TargetFive fetchOneById(String value) {
        return fetchOne(TargetFive.TARGET_FIVE.ID, value);
    }
}