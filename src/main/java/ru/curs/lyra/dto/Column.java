package ru.curs.lyra.dto;

/**
 * Column
 */
public final class Column {

    private String id;
    private String caption;
    private boolean visible;
    private String cssClassName;
    private String cssStyle;
    private boolean sortingAvailable;

    /**
     * getId
     */
    public String getId() {
        return id;
    }

    /**
     * setId
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * getCaption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * setCaption
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }


    /**
     * isVisible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * setVisible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * getCssClassName
     */
    public String getCssClassName() {
        return cssClassName;
    }

    /**
     * setCssClassName
     */
    public void setCssClassName(String cssClassName) {
        this.cssClassName = cssClassName;
    }

    /**
     * getCssStyle
     */
    public String getCssStyle() {
        return cssStyle;
    }

    /**
     * setCssStyle
     */
    public void setCssStyle(String cssStyle) {
        this.cssStyle = cssStyle;
    }

    /**
     * isSortingAvailable
     */
    public boolean isSortingAvailable() {
        return sortingAvailable;
    }

    /**
     * setSortingAvailable
     */
    public void setSortingAvailable(boolean sortingAvailable) {
        this.sortingAvailable = sortingAvailable;
    }
}
