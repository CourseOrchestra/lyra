package ru.curs.lyra.kernel;

import java.text.DateFormat;

/**
 * Lyra form field metadata.
 */
public class LyraFormField extends LyraNamedElement {
    /**
     * 'Visible' property name.
     */
    public static final String VISIBLE = "visible";
    /**
     * 'Editable' property name.
     */
    public static final String EDITABLE = "editable";
    /**
     * 'Sortable' property name.
     */
    public static final String SORTABLE = "sortable";
    /**
     * 'Caption' property name.
     */
    public static final String CAPTION = "caption";

    /**
     * 'Scale' property name.
     */
    public static final String SCALE = "scale";

    /**
     * 'Required' property name.
     */
    public static final String REQUIRED = "required";

    // adding_field's_property
    /**
     * 'cssClassName' property name.
     */
    public static final String CSS_CLASS_NAME = "cssClassName";
    /**
     * 'cssStyle' property name.
     */
    public static final String CSS_STYLE = "cssStyle";
    /**
     * 'dateFormat' property name.
     */
    public static final String DATE_FORMAT = "dateFormat";
    /**
     * 'decimalSeparator' property name.
     */
    public static final String DECIMAL_SEPARATOR = "decimalSeparator";
    /**
     * 'groupingSeparator' property name.
     */
    public static final String GROUPING_SEPARATOR = "groupingSeparator";


    /**
     * Значение по умолчанию для числа знаков после запятой.
     */
    public static final int DEFAULT_SCALE = 2;
    public static final int DEFAULT_DATE_FORMAT = DateFormat.SHORT;
    public static final String DEFAULT_DECIMAL_SEPARATOR = ",";
    public static final String DEFAULT_GROUPING_SEPARATOR = " ";


    private final transient FieldAccessor accessor;

    private LyraFieldType type = LyraFieldType.VARCHAR;
    private boolean editable;
    private boolean sortable;
    private boolean visible;
    private boolean required;
    private String caption;
    private int scale = DEFAULT_SCALE;
    private String lookup;

    // adding_field's_property
    private String cssClassName;
    private String cssStyle;
    private int dateFormat = DEFAULT_DATE_FORMAT;
    private String decimalSeparator = DEFAULT_DECIMAL_SEPARATOR;
    private String groupingSeparator = DEFAULT_GROUPING_SEPARATOR;


    public String getCssClassName() {
        return cssClassName;
    }

    public LyraFormField setCssClassName(String cssClassName) {
        this.cssClassName = cssClassName;
        return this;
    }

    /**
     * Get CssStyle.
     */
    public String getCssStyle() {
        return cssStyle;
    }

    public LyraFormField setCssStyle(String cssStyle) {
        this.cssStyle = cssStyle;
        return this;
    }

    public LyraFormField(String name) {
        super(name);
        accessor = null;
    }

    public LyraFormField(String name, FieldAccessor accessor) {
        super(name);
        this.accessor = accessor;
    }

    /**
     * Field type.
     */
    public LyraFieldType getType() {
        return type;
    }

    /**
     * Sets field type.
     *
     * @param type the type to set
     */
    public LyraFormField setType(LyraFieldType type) {
        this.type = type;
        return this;
    }

    /**
     * Is the field editable?
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets editable property.
     *
     * @param editable editable property.
     */
    public LyraFormField setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    /**
     * Is the field sortable?
     */
    public boolean isSortable() {
        return sortable;
    }

    /**
     * Sets sortable property.
     *
     * @param sortable sortable property.
     */
    public LyraFormField setSortable(boolean sortable) {
        this.sortable = sortable;
        return this;
    }

    /**
     * Is the field visible?
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets visible property.
     *
     * @param visible the visible to set
     */
    public LyraFormField setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    /**
     * Caption of the field.
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Sets new caption.
     *
     * @param caption the caption to set
     */
    public LyraFormField setCaption(String caption) {
        this.caption = caption;
        return this;
    }

    /**
     * Lookup procedure.
     */
    public String getLookup() {
        return lookup;
    }

    /**
     * Sets lookup procedure.
     *
     * @param lookup the lookup procedure to set
     */
    public LyraFormField setLookup(String lookup) {
        this.lookup = lookup;

        return this;

    }

    /**
     * Field's getter/setter.
     */
    public FieldAccessor getAccessor() {
        return accessor;
    }

    /**
     * Number of decimal places after dot.
     */
    public int getScale() {
        return scale;
    }

    /**
     * Sets number of decimal places after dot.
     *
     * @param scale new value.
     */
    public LyraFormField setScale(int scale) {
        this.scale = scale;
        return this;

    }

    /**
     * Is the field required.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets required property for a field.
     *
     * @param required new value
     */
    public LyraFormField setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public int getDateFormat() {
        return dateFormat;
    }

    public LyraFormField setDateFormat(int dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    public String getDecimalSeparator() {
        return decimalSeparator;
    }

    public LyraFormField setDecimalSeparator(String decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
        return this;
    }

    public String getGroupingSeparator() {
        return groupingSeparator;
    }

    public LyraFormField setGroupingSeparator(String groupingSeparator) {
        this.groupingSeparator = groupingSeparator;
        return this;
    }

}
