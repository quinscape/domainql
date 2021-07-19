package de.quinscape.domainql;

import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.logicimpl.ConfigureNonDBByNameLogic;
import de.quinscape.domainql.testdomain.Public;
import de.quinscape.domainql.testdomain.tables.pojos.Bar;
import de.quinscape.domainql.testdomain.tables.pojos.BarOrg;
import de.quinscape.domainql.testdomain.tables.pojos.BarOwner;
import de.quinscape.domainql.testdomain.tables.pojos.Foo;
import org.junit.Test;
import org.svenson.JSON;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static de.quinscape.domainql.testdomain.Tables.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DomainQLNamingTest
{
}
