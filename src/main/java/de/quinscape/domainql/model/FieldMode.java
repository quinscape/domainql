package de.quinscape.domainql.model;

/**
 * Field mode enum analog to FieldMode.js in the domainql-form JavaScript companion library.
 */
public enum FieldMode
{
    // XXX: Keep enum values in sync with domainql-form/src/FieldMode.js
    /**
     * Normal editable field.
     */
    NORMAL,

    /**
     * Field is disabled.
     */
    DISABLED,

    /**
     * Field is set readOnly where that is applicable, otherwise it causes the same behavior as {@link #DISABLED}.
     */
    READ_ONLY,

    /**
     * Field is rendered as plain-text value
     */
    PLAIN_TEXT,

    /**
     * The complete field group of the field including label, potential columns, help-texts is not rendered.
     */
    INVISIBLE
}
