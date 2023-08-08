package de.quinscape.domainql.config;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.TypeRegistry;
import org.jooq.Table;
import org.jooq.TableField;
import org.svenson.JSONProperty;

import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * DomainQL configuration for a single relation. The relation can be either be based on an actual database foreign
 * key or
 * on POJO fields (for database views).
 *
 * @see de.quinscape.domainql.RelationBuilder
 */
public class RelationModel
{
    private final List<String> targetFields;

    private final SourceField sourceField;

    private final TargetField targetField;

    private final String leftSideObjectName;

    private final String rightSideObjectName;

    private final String id;

    private final Table<?> sourceTable;

    private final Class<?> sourcePojoClass;

    private final List<? extends TableField<?, ?>> sourceDBFields;

    private final List<String> sourceFields;

    private final Table<?> targetTable;

    private final Class<?> targetPojoClass;

    private final List<? extends TableField<?, ?>> targetDBFields;

    private final String targetType;

    private final List<String> metaTags;

    private final String sourceType;


    public RelationModel(
        String id,
        @NotNull Table<?> sourceTable,
        @NotNull Class<?> sourcePojoClass,
        @NotNull List<? extends TableField<?, ?>> sourceDBFields,
        @NotNull List<String> sourceFields,
        @NotNull Table<?> targetTable,
        @NotNull Class<?> targetPojoClass,
        @NotNull List<? extends TableField<?, ?>> targetDBFields,
        @NotNull List<String> targetFields,
        @NotNull SourceField sourceField,
        @NotNull TargetField targetField,
        String leftSideObjectName,
        String rightSideObjectName,
        List<String> tags
    )
    {
        this.id = id;
        this.sourceTable = sourceTable;
        this.sourcePojoClass = sourcePojoClass;
        this.sourceDBFields = sourceDBFields;
        this.sourceFields = sourceFields;
        this.targetTable = targetTable;
        this.targetPojoClass = targetPojoClass;
        this.targetDBFields = targetDBFields;
        this.targetFields = targetFields;
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.leftSideObjectName = leftSideObjectName;
        this.rightSideObjectName = rightSideObjectName;

        this.sourceType = sourcePojoClass.getSimpleName();
        this.targetType = targetPojoClass.getSimpleName();
        this.metaTags = Collections.unmodifiableList(tags);
    }


    @JSONProperty(ignore = true)
    public Table<?> getSourceTable()
    {
        return sourceTable;
    }


    @JSONProperty(ignore = true)
    public List<? extends TableField<?, ?>> getSourceDBFields()
    {
        return sourceDBFields;
    }


    @JSONProperty(ignore = true)
    public Class<?> getSourcePojoClass()
    {
        return sourcePojoClass;
    }


    @JSONProperty(ignore = true)
    public Class<?> getTargetPojoClass()
    {
        return targetPojoClass;
    }


    @JSONProperty(ignore = true)
    public Table<?> getTargetTable()
    {
        return targetTable;
    }


    @JSONProperty(ignore = true)
    public List<? extends TableField<?, ?>> getTargetDBFields()
    {
        return targetDBFields;
    }


    public String getSourceType()
    {
        return sourceType;
    }


    public String getTargetType()
    {
        return targetType;
    }


    public List<String> getSourceFields()
    {
        return sourceFields;
    }


    public List<String> getTargetFields()
    {
        return targetFields;
    }


    public SourceField getSourceField()
    {
        return sourceField;
    }


    public TargetField getTargetField()
    {
        return targetField;
    }


    @JSONProperty(ignoreIfNull = true)
    public String getLeftSideObjectName()
    {
        return leftSideObjectName;
    }


    @JSONProperty(ignoreIfNull = true)
    public String getRightSideObjectName()
    {
        return rightSideObjectName;
    }


    @JSONProperty(ignoreIfNull = true)
    public String getId()
    {
        return id;
    }


    /**
     * Updates the current relation model to contain the correct overloaded types in case type overloading happened for
     * either source or target POJO.
     *
     * @param domainQL      DomainQL instance
     *
     * @return new relation model with updated type reference or the same relation model if no updates where necessary
     */
    public RelationModel update(DomainQL domainQL)
    {
        final TypeRegistry typeRegistry = domainQL.getTypeRegistry();

        final Class<?> realSource = typeRegistry.getOutputOverride(sourcePojoClass);
        final Class<?> realTarget = typeRegistry.getOutputOverride(targetPojoClass);

        if (
            sourcePojoClass.getName().equals(realSource.getName()) &&
            targetPojoClass.getName().equals(realTarget.getName())
        )
        {
            return this;
        }

        return new RelationModel(
            id,
            sourceTable,
            realSource,
            sourceDBFields,
            sourceFields,
            targetTable,
            realTarget,
            targetDBFields,
            targetFields,
            sourceField,
            targetField,
            leftSideObjectName,
            rightSideObjectName,
            metaTags
        );
    }


    public List<String> getMetaTags()
    {
        return metaTags;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "id = '" + id + '\''
            + ", targetFields = " + targetFields
            + ", sourceField = " + sourceField
            + ", targetField = " + targetField
            + ", leftSideObjectName = '" + leftSideObjectName + '\''
            + ", rightSideObjectName = '" + rightSideObjectName + '\''
            + ", sourceTable = " + sourceTable
            + ", sourcePojoClass = " + sourcePojoClass
            + ", sourceDBFields = " + sourceDBFields
            + ", sourceFields = " + sourceFields
            + ", targetTable = " + targetTable
            + ", targetPojoClass = " + targetPojoClass
            + ", targetDBFields = " + targetDBFields
            + ", targetType = '" + targetType + '\''
            + ", sourceType = '" + sourceType + '\''
            ;
    }
}
