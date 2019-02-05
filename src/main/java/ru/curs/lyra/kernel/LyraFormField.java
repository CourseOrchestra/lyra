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

    public void setCssClassName(String cssClassName) {
        this.cssClassName = cssClassName;
    }

    public String getCssStyle() {
        return cssStyle;
    }

    public void setCssStyle(String cssStyle) {
        this.cssStyle = cssStyle;
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
    public void setType(LyraFieldType type) {
        this.type = type;
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
    public void setEditable(boolean editable) {
        this.editable = editable;
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
    public void setVisible(boolean visible) {
        this.visible = visible;
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
    public void setCaption(String caption) {
        this.caption = caption;
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
    public void setLookup(String lookup) {
        this.lookup = lookup;
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
    public void setScale(int scale) {
        this.scale = scale;
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
    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(int dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDecimalSeparator() {
        return decimalSeparator;
    }

    public void setDecimalSeparator(String decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    public String getGroupingSeparator() {
        return groupingSeparator;
    }

    public void setGroupingSeparator(String groupingSeparator) {
        this.groupingSeparator = groupingSeparator;
    }

}
