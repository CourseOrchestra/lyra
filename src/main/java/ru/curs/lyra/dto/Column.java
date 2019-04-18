package ru.curs.lyra.dto;


public class Column {

    private String id;
    private String caption;
    private boolean visible;
    private String cssClassName;
    private String cssStyle;
    private boolean sortingAvailable;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

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

    public boolean isSortingAvailable() {
        return sortingAvailable;
    }

    public void setSortingAvailable(boolean sortingAvailable) {
        this.sortingAvailable = sortingAvailable;
    }
}
