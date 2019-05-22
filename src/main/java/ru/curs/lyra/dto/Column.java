package ru.curs.lyra.dto;

/**
 * Column.
 */
public final class Column {

    private String id;
    private String caption;
    private boolean visible;
    private boolean sortable;
    private String cssClassName;
    private String cssStyle;
    private boolean sortingAvailable;

    /**
     * getId.
     */
    public String getId() {
        return id;
    }

    /**
     * setId.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * getCaption.
     */
    public String getCaption() {
        return caption;
    }

    /**
     * setCaption.
     *
     * @param caption caption
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }


    /**
     * isVisible.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * setVisible.
     *
     * @param visible visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * getCssClassName.
     */
    public String getCssClassName() {
        return cssClassName;
    }

    /**
     * setCssClassName.
     *
     * @param cssClassName cssClassName
     */
    public void setCssClassName(String cssClassName) {
        this.cssClassName = cssClassName;
    }

    /**
     * getCssStyle.
     */
    public String getCssStyle() {
        return cssStyle;
    }

    /**
     * setCssStyle.
     *
     * @param cssStyle cssStyle
     */
    public void setCssStyle(String cssStyle) {
        this.cssStyle = cssStyle;
    }

    /**
     * isSortingAvailable.
     */
    public boolean isSortingAvailable() {
        return sortingAvailable;
    }

    /**
     * setSortingAvailable.
     *
     * @param sortingAvailable sortingAvailable
     */
    public void setSortingAvailable(boolean sortingAvailable) {
        this.sortingAvailable = sortingAvailable;
    }


    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

}
