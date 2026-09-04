package de.quinscape.domainql.logicimpl;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.beans.MarkdownDocumentedBean;

/// Target to be analyzed by DocsExtractorTest
///
/// @see de.quinscape.domainql.docs.DocsExtractorTest
@GraphQLLogic
public class MarkdownDocumentedLogic
{
    /// A minimal markdown GraphQL query
    ///
    /// @return always true
    @GraphQLQuery
    public boolean markdownQuery()
    {
        return true;
    }


    /// Another markdown query
    @GraphQLQuery(value = "anotherMarkdownQuery")
    public MarkdownDocumentedBean markdownQuery2(MarkdownDocumentedBean bean)
    {
        return bean;
    }


    /// A markdown GraphQL mutation
    ///
    /// Supports:
    ///
    /// * one
    /// * two
    ///
    /// @param foo   foo param desc
    /// @return always false
    @GraphQLMutation
    public boolean markdownMutation(int foo)
    {
        return false;
    }
}
