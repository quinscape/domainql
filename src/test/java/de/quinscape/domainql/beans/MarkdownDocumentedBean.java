package de.quinscape.domainql.beans;

import de.quinscape.domainql.annotation.GraphQLField;
import de.quinscape.domainql.docs.DocsExtractorTest;

/// Markdown target for [DocsExtractorTest].
///
/// Supports:
///
/// * bullet one
/// * bullet two
///
/// Inline tags like {@link DocsExtractorTest} are still stripped.
public class MarkdownDocumentedBean
{
    private String name;
    private int num;


    /// Name desc from getter
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public int getNum()
    {
        return num;
    }


    /// Num desc from setter
    public void setNum(int num)
    {
        this.num = num;
    }


    /// Field with args
    ///
    /// @param name  name desc
    /// @param num   num desc
    @GraphQLField
    public int getFieldWithArgs(
        String name,
        int num
    )
    {
        return name.hashCode() + num;
    }


    /// SECTION MARKER, not documentation

    public String getUndocumented()
    {
        return null;
    }
}
