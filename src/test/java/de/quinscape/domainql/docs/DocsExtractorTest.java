package de.quinscape.domainql.docs;

import com.github.javaparser.utils.SourceRoot;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

    @Test
    public void testMerging() throws IOException
    {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("./src/test/java/"));

        final DocsExtractor docsExtractor = new DocsExtractor();

        final TypeDoc otherDoc = new TypeDoc("DocumentedBean");
        otherDoc.setDescription("Changed description");
        otherDoc.setFieldDocs(Arrays.asList(new FieldDoc("name", "changed name desc")));
        final List<TypeDoc> docs = docsExtractor.extract(sourceRoot,"", "de/quinscape/domainql/beans/DocumentedBean.java",
            Arrays.asList(
                otherDoc
            ));

        assertThat(docs.size(), is(1));

        final Iterator<TypeDoc> i = docs.iterator();

        final TypeDoc beanDoc = i.next();


        assertThat(beanDoc.getName(), is("DocumentedBean"));

        // {@link ... } are stripped
        assertThat(beanDoc.getDescription(), is("Changed description"));

        final List<FieldDoc> queryFields = beanDoc.getFieldDocs();
        assertThat(queryFields.size(), is(3));
        assertThat(queryFields.get(0).getName(), is("getFieldWithArgs"));
        assertThat(queryFields.get(0).getDescription(), is("Field with args"));
        assertThat(queryFields.get(0).getParamDocs().size(), is(2));
        assertThat(queryFields.get(0).getParamDocs().get(0).getName(), is("name"));
        assertThat(queryFields.get(0).getParamDocs().get(0).getDescription(), is("name desc"));
        assertThat(queryFields.get(0).getParamDocs().get(1).getName(), is("num"));
        assertThat(queryFields.get(0).getParamDocs().get(1).getDescription(), is("num desc"));

        assertThat(queryFields.get(1).getName(), is("name"));
        assertThat(queryFields.get(1).getDescription(), is("changed name desc"));
        assertThat(queryFields.get(1).getParamDocs().size(), is(0));
        assertThat(queryFields.get(1).getParamDocs().size(), is(0));

        assertThat(queryFields.get(2).getName(), is("num"));
        assertThat(queryFields.get(2).getDescription(), is("Num desc from setter"));
        assertThat(queryFields.get(2).getParamDocs().size(), is(0));
        assertThat(queryFields.get(2).getParamDocs().size(), is(0));

    }



    @Test
    public void testMarkdownLogicDocExtraction() throws IOException
    {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("./src/test/java/"));

        final DocsExtractor docsExtractor = new DocsExtractor();

        final List<TypeDoc> docs = docsExtractor.extract(sourceRoot,"", "de/quinscape/domainql/logicimpl/MarkdownDocumentedLogic.java");

        assertThat(docs.size(), is(2));

        final TypeDoc qDoc = docs.stream().filter( doc -> doc.getName().equals(TypeDoc.QUERY_TYPE)).findFirst().get();
        final TypeDoc mDoc = docs.stream().filter( doc -> doc.getName().equals(TypeDoc.MUTATION_TYPE)).findFirst().get();

        final List<FieldDoc> queryFields = qDoc.getFieldDocs();
        assertThat(queryFields.size(), is(2));
        assertThat(queryFields.get(0).getName(), is("markdownQuery"));
        assertThat(queryFields.get(0).getDescription(), is("A minimal markdown GraphQL query"));
        assertThat(queryFields.get(0).getParamDocs().size(), is(0));

        // the @GraphQLQuery name override still wins over the method name
        assertThat(queryFields.get(1).getName(), is("anotherMarkdownQuery"));
        assertThat(queryFields.get(1).getDescription(), is("Another markdown query"));
        assertThat(queryFields.get(1).getParamDocs().size(), is(0));

        final List<FieldDoc> mutationFields = mDoc.getFieldDocs();
        assertThat(mutationFields.size(), is(1));
        assertThat(mutationFields.get(0).getName(), is("markdownMutation"));

        // markdown paragraphs and bullet lists survive verbatim
        assertThat(
            mutationFields.get(0).getDescription(),
            is("A markdown GraphQL mutation\n\nSupports:\n\n* one\n* two")
        );
        assertThat(mutationFields.get(0).getParamDocs().size(), is(1));
        assertThat(mutationFields.get(0).getParamDocs().get(0).getName(), is("foo"));
        assertThat(mutationFields.get(0).getParamDocs().get(0).getDescription(), is("foo param desc"));
    }


    @Test
    public void testMarkdownPojoDocExtraction() throws IOException
    {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("./src/test/java/"));

        final DocsExtractor docsExtractor = new DocsExtractor();

        final List<TypeDoc> docs = docsExtractor.extract(sourceRoot,"", "de/quinscape/domainql/beans/MarkdownDocumentedBean.java");

        assertThat(docs.size(), is(1));

        final TypeDoc beanDoc = docs.iterator().next();

        assertThat(beanDoc.getName(), is("MarkdownDocumentedBean"));

        // markdown structure is kept, [reference links] are kept, {@link ... } are stripped
        assertThat(
            beanDoc.getDescription(),
            is(
                "Markdown target for [DocsExtractorTest].\n" +
                    "\n" +
                    "Supports:\n" +
                    "\n" +
                    "* bullet one\n" +
                    "* bullet two\n" +
                    "\n" +
                    "Inline tags like DocsExtractorTest are still stripped."
            )
        );

        final List<FieldDoc> fieldDocs = beanDoc.getFieldDocs();
        assertThat(fieldDocs.size(), is(3));

        final FieldDoc withArgs = fieldDoc(fieldDocs, "getFieldWithArgs");
        assertThat(withArgs.getDescription(), is("Field with args"));
        assertThat(withArgs.getParamDocs().size(), is(2));
        assertThat(withArgs.getParamDocs().get(0).getName(), is("name"));
        assertThat(withArgs.getParamDocs().get(0).getDescription(), is("name desc"));
        assertThat(withArgs.getParamDocs().get(1).getName(), is("num"));
        assertThat(withArgs.getParamDocs().get(1).getDescription(), is("num desc"));

        assertThat(fieldDoc(fieldDocs, "name").getDescription(), is("Name desc from getter"));
        assertThat(fieldDoc(fieldDocs, "num").getDescription(), is("Num desc from setter"));

        // a decorative /// marker separated by an empty line does not document the next method
        assertThat(
            fieldDocs.stream().anyMatch(doc -> doc.getName().equals("undocumented")),
            is(false)
        );
    }


    @Test
    public void testMarkdownEnumDocExtraction() throws IOException
    {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("./src/test/java/"));

        final DocsExtractor docsExtractor = new DocsExtractor();

        final List<TypeDoc> docs = docsExtractor.extract(sourceRoot,"", "de/quinscape/domainql/beans/MarkdownDocumentedEnum.java");

        assertThat(docs.size(), is(1));

        final TypeDoc enumDoc = docs.iterator().next();

        assertThat(enumDoc.getName(), is("MarkdownDocumentedEnum"));
        assertThat(enumDoc.getDescription(), is("A markdown documented enum"));

        final List<FieldDoc> fieldDocs = enumDoc.getFieldDocs();
        assertThat(fieldDocs.size(), is(2));
        assertThat(fieldDocs.get(0).getName(), is("VALUE_ONE"));
        assertThat(fieldDocs.get(0).getDescription(), is("The *first* value"));
        assertThat(fieldDocs.get(1).getName(), is("VALUE_TWO"));
        assertThat(fieldDocs.get(1).getDescription(), is("The *second* value"));
    }


    private FieldDoc fieldDoc(List<FieldDoc> fieldDocs, String name)
    {
        return fieldDocs.stream()
            .filter(doc -> doc.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No field doc '" + name + "' in " + fieldDocs));
    }


}
