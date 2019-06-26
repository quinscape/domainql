package de.quinscape.domainql.config;

/**
 * DomainQL configuration for a single relation / foreign key
 */
public class RelationConfiguration
{
    private final SourceField sourceField;
    private final TargetField targetField;

    private final String leftSideObjectName;
    private final String rightSideObjectName;


    public RelationConfiguration(
        SourceField sourceField, TargetField targetField
    )
    {
        this(sourceField, targetField, null, null);
    }
    public RelationConfiguration(
        SourceField sourceField, TargetField targetField, String leftSideObjectName, String rightSideObjectName
    )
    {
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.leftSideObjectName = leftSideObjectName;
        this.rightSideObjectName = rightSideObjectName;
    }



    public SourceField getSourceField()
    {
        return sourceField;
    }


    public TargetField getTargetField()
    {
        return targetField;
    }


    public String getLeftSideObjectName()
    {
        return leftSideObjectName;
    }


    public String getRightSideObjectName()
    {
        return rightSideObjectName;
    }
}
