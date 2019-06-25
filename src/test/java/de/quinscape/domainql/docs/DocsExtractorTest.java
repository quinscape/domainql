package de.quinscape.domainql.docs;

import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.ImmutableSet;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DocsExtractorTest
{
    private final static Logger log = LoggerFactory.getLogger(DocsExtractorTest.class);


    @Test
    public void testLogicDocExtraction() throws IOException
    {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("./src/test/java/"));

        final DocsExtractor docsExtractor = new DocsExtractor();

        final List<TypeDoc> docs = docsExtractor.extract(sourceRoot,"", "de/quinscape/domainql/logicimpl/DocumentedLogic.java");

        assertThat(docs.size(), is(2));

        final TypeDoc qDoc = docs.stream().filter( doc -> doc.getName().equals(TypeDoc.QUERY_TYPE)).findFirst().get();
        final TypeDoc mDoc = docs.stream().filter( doc -> doc.getName().equals(TypeDoc.MUTATION_TYPE)).findFirst().get();


        final List<FieldDoc> queryFields = qDoc.getFieldDocs();
        assertThat(queryFields.size(), is(3));
        assertThat(queryFields.get(0).getName(), is("query"));
        assertThat(queryFields.get(0).getDescription(), is("A minimal GraphQL query"));
        assertThat(queryFields.get(0).getParamDocs().size(), is(0));

        assertThat(queryFields.get(1).getName(), is("anotherQuery"));
        assertThat(queryFields.get(1).getDescription(), is("Another query"));
        assertThat(queryFields.get(1).getParamDocs().size(), is(0));

        final List<FieldDoc> mutationFields = mDoc.getFieldDocs();
        assertThat(mutationFields.size(), is(1));
        assertThat(mutationFields.get(0).getName(), is("mutation"));
        assertThat(mutationFields.get(0).getDescription(), is("A GraphQL mutation"));
        assertThat(mutationFields.get(0).getParamDocs().size(), is(1));
        assertThat(mutationFields.get(0).getParamDocs().get(0).getName(), is("foo"));
        assertThat(mutationFields.get(0).getParamDocs().get(0).getDescription(), is("foo param desc"));

    }


    @Test
    public void testPojoDocExtraction() throws IOException
    {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("./src/test/java/"));

        final DocsExtractor docsExtractor = new DocsExtractor();

        final List<TypeDoc> docs = docsExtractor.extract(sourceRoot,"", "de/quinscape/domainql/beans/DocumentedBean.java");

        assertThat(docs.size(), is(1));

        final Iterator<TypeDoc> i = docs.iterator();

        final TypeDoc beanDoc = i.next();


        assertThat(beanDoc.getName(), is("DocumentedBean"));

        // {@link ... } are stripped
        assertThat(beanDoc.getDescription(), is("Target for DocsExtractorTest"));

        final List<FieldDoc> queryFields = beanDoc.getFieldDocs();
        assertThat(queryFields.size(), is(3));
        assertThat(queryFields.get(0).getName(), is("getFieldWithArgs"));
        assertThat(queryFields.get(0).getDescription(), is("Field with args"));
        assertThat(queryFields.get(0).getParamDocs().size(), is(2));
        assertThat(queryFields.get(0).getParamDocs().get(0).getName(), is("name"));
        assertThat(queryFields.get(0).getParamDocs().get(0).getDescription(), is("name desc"));
        assertThat(queryFields.get(0).getParamDocs().get(1).getName(), is("num"));
        assertThat(queryFields.get(0).getParamDocs().get(1).getDescription(), is("num desc"));

        assertThat(queryFields.get(1).getName(), is("num"));
        assertThat(queryFields.get(1).getDescription(), is("Num desc from setter"));
        assertThat(queryFields.get(1).getParamDocs().size(), is(0));
        assertThat(queryFields.get(1).getParamDocs().size(), is(0));

        assertThat(queryFields.get(2).getName(), is("name"));
        assertThat(queryFields.get(2).getDescription(), is("Name desc from getter"));
        assertThat(queryFields.get(2).getParamDocs().size(), is(0));
        assertThat(queryFields.get(2).getParamDocs().size(), is(0));
    }


}
