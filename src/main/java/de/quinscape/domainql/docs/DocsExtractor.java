package de.quinscape.domainql.docs;

import com.beust.jcommander.JCommander;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.utils.SourceRoot;
import de.quinscape.domainql.annotation.GraphQLField;
import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.annotation.GraphQLQuery;
import org.apache.commons.io.FileUtils;
import org.svenson.JSON;
import org.svenson.JSONParser;
import org.svenson.tokenize.InputStreamSource;

import java.beans.Introspector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extracts GraphQL-centric JSON documentation from Java source files.
 */
public class DocsExtractor
{

    public List<TypeDoc> extract(SourceRoot sourceRoot, Set<String> basePackages) throws IOException
    {
        return extract(sourceRoot, basePackages, null);
    }

    public List<TypeDoc> extract(SourceRoot sourceRoot, Set<String> basePackages, List<TypeDoc> otherDocs) throws IOException
    {
        final List<TypeDoc> docs = new ArrayList<>();
        for (String pkg : basePackages)
        {
            sourceRoot.parse(pkg, (localPath, absolutePath, result) -> {

                if (result.isSuccessful())
                {
                    final List<TypeDoc> docsForFile = extract(result);
                    docs.addAll(docsForFile);
                }

                return SourceRoot.Callback.Result.DONT_SAVE;
            });
        }

        if (otherDocs != null)
        {
            docs.addAll(otherDocs);
        }

        return normalize(docs);
    }


    public List<TypeDoc> extract(SourceRoot sourceRoot, String basePackage, String fileName) throws IOException
    {
        return extract(sourceRoot, basePackage, fileName, null);
    }

    public List<TypeDoc> extract(SourceRoot sourceRoot, String basePackage, String fileName, List<TypeDoc> otherDocs) throws IOException
    {
        final DocsExtractor docsExtractor = new DocsExtractor();

        final List<TypeDoc> docs = new ArrayList<>();
        sourceRoot.parse(basePackage, fileName, (localPath, absolutePath, result) -> {

            if (result.isSuccessful())
            {
                docs.addAll(docsExtractor.extract(result));
            }
            return SourceRoot.Callback.Result.DONT_SAVE;
        });

        if (otherDocs != null)
        {
            docs.addAll(otherDocs);
            return DocsExtractor.normalize(docs);
        }
        return docs;
    }


    List<TypeDoc> extract(ParseResult<CompilationUnit> result)
    {
        final CompilationUnit unit = result.getResult().get();

        if (unit.getTypes().size() > 0)
        {
            TypeDeclaration<?> typeDecl = unit.getType(0);
            final Optional<AnnotationExpr> logicAnno = getAnnotation(typeDecl, GraphQLLogic.class);
            if (logicAnno.isPresent())
            {
                return extractLogicClassDocumentation(typeDecl);
            }
            else
            {
                return extractPojoDocumentation(typeDecl);
            }
        }
        else
        {
            return Collections.emptyList();
        }
    }


    private List<TypeDoc> extractPojoDocumentation(TypeDeclaration<?> typeDecl)
    {
        if (!typeDecl.isTypeDeclaration())
        {
            return Collections.emptyList();
        }

        final TypeDoc typeDoc = new TypeDoc(typeDecl.getName().getIdentifier());
        if (typeDecl.getJavadoc().isPresent())
        {
            final String description = cleanDescription(typeDecl.getJavadoc().get().getDescription().toText());
            typeDoc.setDescription(description);

        }

        final List<FieldDoc> fieldDocs = new ArrayList<>();
        if (typeDecl.isEnumDeclaration())
        {
            EnumDeclaration enumDeclaration = (EnumDeclaration) typeDecl;

            for (EnumConstantDeclaration decl : enumDeclaration.getEntries())
            {
                if (decl.getJavadoc().isPresent())
                {
                    final String description = decl.getJavadoc().get().getDescription().toText();

                    fieldDocs.add(
                        new FieldDoc(decl.getName().getIdentifier(), description)
                    );
                }
            }
        }
        else
        {
            final Map<String, String> fieldMap = new HashMap<>();

            for (MethodDeclaration method : typeDecl.getMethods())
            {
                final String methodName = method.getName().getIdentifier();

                final NodeList<Parameter> params = method.getParameters();

                final Optional<Javadoc> javadoc = method.getJavadoc();
                if (javadoc.isPresent())
                {
                    final String methodJavaDoc = cleanDescription(javadoc.get().getDescription().toText());
                    if (methodName.startsWith("get") && params.size() == 0)
                    {
                        final String propertyName = Introspector.decapitalize(methodName.substring(3));
                        fieldMap.put(propertyName, methodJavaDoc);
                    }
                    else if (methodName.startsWith("is") && params.size() == 0)
                    {
                        final String propertyName = Introspector.decapitalize(methodName.substring(2));
                        fieldMap.put(propertyName, methodJavaDoc);
                    }
                    else if (methodName.startsWith("set") && method.getType().isVoidType())
                    {
                        final String propertyName = Introspector.decapitalize(methodName.substring(3));
                        fieldMap.put(propertyName, methodJavaDoc);
                    }
                    else
                    {
                        final Optional<AnnotationExpr> anno = getAnnotation(method, GraphQLField.class);
                        if (anno.isPresent())
                        {

                            final String name = getAttribute(anno.get(), "value", method.getName().getIdentifier());
                            final String description = getAttribute(anno.get(), "description", methodJavaDoc);

                            final FieldDoc fieldDoc = new FieldDoc(
                                name,
                                description
                            );

                            if (params.size() > 0)
                            {
                                fieldDoc.setParamDocs(
                                    extractParamDocs(javadoc.get())
                                );
                            }

                            fieldDocs.add(
                                fieldDoc
                            );
                        }
                    }
                }
            }

            for (Map.Entry<String, String> e : fieldMap.entrySet())
            {
                final String fieldName = e.getKey();
                final String description = e.getValue();

                fieldDocs.add(
                    new FieldDoc(fieldName, description)
                );

            }

            typeDoc.setFieldDocs(fieldDocs);

            if (typeDoc.getDescription() == null && fieldDocs.size() == 0)
            {
                return Collections.emptyList();
            }
        }
        typeDoc.setFieldDocs(fieldDocs);

        return Collections.singletonList(typeDoc);
    }


    private List<TypeDoc> extractLogicClassDocumentation(TypeDeclaration<?> typeDecl)
    {
        final TypeDoc queryType = new TypeDoc(TypeDoc.QUERY_TYPE);
        final TypeDoc mutationType = new TypeDoc(TypeDoc.MUTATION_TYPE);

        List<TypeDoc> typeDocs = new ArrayList<>();

        for (MethodDeclaration method : typeDecl.getMethods())
        {
            if (method.getJavadoc().isPresent())
            {
                final Javadoc methodJavaDoc = method.getJavadoc().get();
                final String methodDescription = cleanDescription(methodJavaDoc.getDescription().toText());
                final Optional<AnnotationExpr> queryAnno = getAnnotation(method, GraphQLQuery.class);
                if (queryAnno.isPresent())
                {

                    final FieldDoc doc = getDocForGraphQLMethod(
                        method,
                        queryAnno.get(),
                        methodJavaDoc,
                        methodDescription
                    );

                    queryType.getFieldDocs().add(doc);
                }
                final Optional<AnnotationExpr> mutationAnno = getAnnotation(method, GraphQLMutation.class);
                if (mutationAnno.isPresent())
                {
                    final FieldDoc doc = getDocForGraphQLMethod(
                        method,
                        mutationAnno.get(),
                        methodJavaDoc,
                        methodDescription
                    );
                    mutationType.getFieldDocs().add(doc);
                }
            }
        }

        if (queryType.getFieldDocs().size() > 0)
        {
            typeDocs.add(queryType);
        }
        if (mutationType.getFieldDocs().size() > 0)
        {
            typeDocs.add(mutationType);
        }

        return typeDocs;
    }


    private FieldDoc getDocForGraphQLMethod(
        MethodDeclaration method,
        AnnotationExpr anno,
        Javadoc methodJavaDoc,
        String methodDescription
    )
    {
        final String name = getAttribute(anno, "value", method.getName().getIdentifier());
        final String description = getAttribute(anno, "description", methodDescription);

        final FieldDoc doc = new FieldDoc(name, description);

        final NodeList<Parameter> params = method.getParameters();
        final int paramCount = params.size();
        if (paramCount > 0)
        {
            doc.setParamDocs(
                extractParamDocs(methodJavaDoc)
            );
        }
        return doc;
    }


    private List<ParamDoc> extractParamDocs(Javadoc methodJavaDoc)
    {
        return methodJavaDoc.getBlockTags().stream()
            .filter(
                bt ->
                    bt.getType() == JavadocBlockTag.Type.PARAM &&
                        bt.getName().isPresent() &&
                        !bt.getName().get().startsWith("<")
            )
            .map(blockTag -> {
                final String paramName = blockTag.getName().get();
                final String paramDesc = cleanDescription(blockTag.getContent().toText());
                return new ParamDoc(paramName, paramDesc);
            })
            .collect(
                Collectors.toList()
            );
    }


    private String getAttribute(AnnotationExpr annotationExpr, String name, String defaultValue)
    {
        for (Node node : annotationExpr.getChildNodes())
        {
            if (node instanceof MemberValuePair)
            {
                MemberValuePair pair = (MemberValuePair) node;
                if (pair.getName().asString().equals(name))
                {
                    final String quoted = pair.getValue().toString();
                    return quoted.substring(1, quoted.length() - 1);
                }
            }
        }

        return defaultValue;
    }


    private Optional<AnnotationExpr> getAnnotation(MethodDeclaration method, Class<?> graphQLQueryClass)
    {
        final Optional<AnnotationExpr> annoForSimpleName =
            method.getAnnotationByName(graphQLQueryClass.getSimpleName());
        if (annoForSimpleName.isPresent())
        {
            return annoForSimpleName;
        }

        return method.getAnnotationByName(graphQLQueryClass.getName());
    }


    private Optional<AnnotationExpr> getAnnotation(TypeDeclaration<?> typeDecl, Class<?> graphQLQueryClass)
    {
        final Optional<AnnotationExpr> annoForSimpleName =
            typeDecl.getAnnotationByName(graphQLQueryClass.getSimpleName());
        if (annoForSimpleName.isPresent())
        {
            return annoForSimpleName;
        }

        return typeDecl.getAnnotationByName(graphQLQueryClass.getName());
    }


    /**
     * Joins all {@link TypeDoc#QUERY_TYPE} and {@link TypeDoc#MUTATION_TYPE} into a single type each.
     * <p>
     * Ensures that type names and field names within the query / mutation types are unique
     * <p>
     * Sorts types and fields to stable, reproducible generation.
     *
     * @param typeDocs
     *
     * @return
     */
    public static List<TypeDoc> normalize(List<TypeDoc> typeDocs)
    {
        TypeDoc queryDoc = new TypeDoc(TypeDoc.QUERY_TYPE);
        TypeDoc mutationDoc = new TypeDoc(TypeDoc.MUTATION_TYPE);

        for (Iterator<TypeDoc> iterator = typeDocs.iterator(); iterator.hasNext(); )
        {
            TypeDoc typeDoc = iterator.next();

            if (typeDoc.getName().equals(TypeDoc.QUERY_TYPE))
            {
                queryDoc.getFieldDocs().addAll(typeDoc.getFieldDocs());
                iterator.remove();
            }
            else if (typeDoc.getName().equals(TypeDoc.MUTATION_TYPE))
            {
                mutationDoc.getFieldDocs().addAll(typeDoc.getFieldDocs());
                iterator.remove();
            }
        }

        if (queryDoc.getFieldDocs().size() > 0)
        {
            typeDocs.add(queryDoc);
        }
        if (mutationDoc.getFieldDocs().size() > 0)
        {
            typeDocs.add(mutationDoc);
        }


        ensureUniqueFields(queryDoc);
        ensureUniqueFields(mutationDoc);
        final List<TypeDoc> unique = mergeTypeDocs(typeDocs);

        // sort types by type name
        unique.sort(TypeDocComparator.INSTANCE);

        unique.forEach( doc -> doc.getFieldDocs().sort(FieldDocComparator.INSTANCE));

        return unique;
    }


    private static void ensureUniqueFields(TypeDoc mutationDoc)
    {
        Set<String> names = new HashSet<>();

        for (FieldDoc fieldDoc : mutationDoc.getFieldDocs())
        {
            final String name = fieldDoc.getName();
            if (names.contains(name))
            {
                throw new IllegalStateException("Field name not unique: '" + name + "'");
            }
            names.add(name);
        }
    }


    /**
     * Removes typedocs with duplicate names so that the last such type doc in iteration order remains.
     *
     * @return new set with unique names
     */
    private static List<TypeDoc> mergeTypeDocs(List<TypeDoc> typeDocs)
    {
        Map<String, TypeDoc> names = new LinkedHashMap<>();
        for (TypeDoc typeDoc : typeDocs)
        {
            final String name = typeDoc.getName();
            final TypeDoc existing = names.put(name, typeDoc);

            if (existing != null)
            {
                final Set<String> fieldNames = typeDoc.getFieldDocs()
                    .stream()
                    .map(FieldDoc::getName)
                    .collect(Collectors.toSet());

                for (FieldDoc fieldDoc : existing.getFieldDocs())
                {
                    if (!fieldNames.contains(fieldDoc.getName()))
                    {
                        final List<FieldDoc> existingFieldDocs = typeDoc.getFieldDocs();

                        if (existingFieldDocs instanceof ArrayList)
                        {
                            existingFieldDocs.add(fieldDoc);
                        }
                        else
                        {
                            final ArrayList<FieldDoc> fieldDocs = new ArrayList<>(existingFieldDocs);
                            fieldDocs.add(fieldDoc);
                            typeDoc.setFieldDocs(fieldDocs);
                        }
                    }
                }
            }
        }
        return new ArrayList<>(names.values());
    }


    static String cleanDescription(String text)
    {
        return text.replaceAll("\\{@link\\s*(.*?)\\}", "$1");
    }


    public static void main(String[] args)
    {
        DocsExtractor main = new DocsExtractor();

        final JCommander commander = JCommander.newBuilder()
            .addObject(main)
            .build();


        try
        {
            commander.parse(args);
            main.run();
        }
        catch (Exception e)
        {
            System.err.println("*ERROR* excuting DocsExtractor");
            e.printStackTrace();
        }

    }


    /// COMMAND LINE USAGE
    @com.beust.jcommander.Parameter(names = {"-s", "--sourceRoot"}, description = "Source root directory", required =
        true)
    public String sourceRoot;

    @com.beust.jcommander.Parameter(names = {"-p", "--package"}, description = "Package to scan for logic " +
        "implementations or beans", required = true)
    public List<String> basePackages;

    @com.beust.jcommander.Parameter(names = {"-o", "--output"}, description = "Writes output the given file. Default " +
        "is print to stdout.")
    public String targetFile;

    @com.beust.jcommander.Parameter(names = {"-m", "--merge"}, description = "Merge javadoc based documentation with documentation from other sources.")
    public String mergeFile;

    @com.beust.jcommander.Parameter(names = {"--pretty"}, description = "Format JSON output")
    public boolean pretty;


    /**
     * Run from command line with CLI configuration
     *
     * @throws IOException
     */
    private void run() throws IOException
    {
        SourceRoot root = new SourceRoot(Paths.get(sourceRoot));

        final DocsExtractor extractor = new DocsExtractor();

        final List<TypeDoc> otherDocs;
        if (mergeFile != null)
        {
            final JSONParser parser = new JSONParser();
            parser.addTypeHint("[]", TypeDoc.class);
            otherDocs = parser.parse(
                List.class,
                new InputStreamSource(
                    new FileInputStream(
                        new File(mergeFile)
                    ),
                    true
                )
            );
        }
        else
        {
            otherDocs = null;
        }

        final List<TypeDoc> typeDocs = extractor.extract(
            root,
            new HashSet<>(basePackages),
            otherDocs
        );

        String json = JSON.defaultJSON().forValue(typeDocs);

        if (pretty)
        {
            json = JSON.formatJSON(json);
        }

        if (targetFile == null || targetFile.length() == 0)
        {
            System.out.println(json);
        }
        else
        {
            FileUtils.writeStringToFile(
                new File(targetFile),
                json,
                "UTF-8"
            );
        }
    }
}
