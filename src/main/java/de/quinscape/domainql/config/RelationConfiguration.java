package de.quinscape.domainql.config;

/**
 * DomainQL configuration for a single relation / foreign key
 */
public class RelationConfiguration
{
    private final SourceField sourceField;
    private final TargetField targetField;

    private final String leftSideName;
    private final String rightSideName;


    public RelationConfiguration(
        SourceField sourceField, TargetField targetField
    )
    {
        this(sourceField, targetField, null, null);
    }
    public RelationConfiguration(
        SourceField sourceField, TargetField targetField, String leftSideName, String rightSideName
    )
    {
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.leftSideName = leftSideName;
        this.rightSideName = rightSideName;
    }



    public SourceField getSourceField()
    {
        return sourceField;
    }


    public TargetField getTargetField()
    {
        return targetField;
    }


    public String getLeftSideName()
    {
        return leftSideName;
    }


    public String getRightSideName()
    {
        return rightSideName;
    }
}
