package ru.curs.lyra.dto;

import java.util.Map;

public class Common {
    private String gridWidth;
    private String gridHeight;
    private int limit;
    private int totalCount;
    private String selectionModel;
    private boolean visibleColumnsHeader;
    private boolean allowTextSelection;
    private String primaryKey;
    private Map<String, String> summaryRow;

    public String getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(String gridWidth) {
        this.gridWidth = gridWidth;
    }

    public String getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(String gridHeight) {
        this.gridHeight = gridHeight;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getSelectionModel() {
        return selectionModel;
    }

    public void setSelectionModel(String selectionModel) {
        this.selectionModel = selectionModel;
    }

    public boolean isVisibleColumnsHeader() {
        return visibleColumnsHeader;
    }

    public void setVisibleColumnsHeader(boolean visibleColumnsHeader) {
        this.visibleColumnsHeader = visibleColumnsHeader;
    }

    public boolean isAllowTextSelection() {
        return allowTextSelection;
    }

    public void setAllowTextSelection(boolean allowTextSelection) {
        this.allowTextSelection = allowTextSelection;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Map<String, String> getSummaryRow() {
        return summaryRow;
    }

    public void setSummaryRow(Map<String, String> summaryRow) {
        this.summaryRow = summaryRow;
    }
}
