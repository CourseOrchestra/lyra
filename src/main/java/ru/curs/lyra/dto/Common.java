package ru.curs.lyra.dto;

import java.util.Map;

/**
 * Common
 */
public final class Common {
    private String gridWidth;
    private String gridHeight;
    private int limit;
    private int totalCount;
    private String selectionModel;
    private boolean visibleColumnsHeader;
    private boolean allowTextSelection;
    private String[] primaryKey;
    private Map<String, String> summaryRow;

    /**
     * getGridWidth.
     */
    public String getGridWidth() {
        return gridWidth;
    }

    /**
     * setGridWidth.
     *
     * @param gridWidth gridWidth
     */
    public void setGridWidth(String gridWidth) {
        this.gridWidth = gridWidth;
    }

    /**
     * getGridHeight.
     */
    public String getGridHeight() {
        return gridHeight;
    }

    /**
     * setGridHeight.
     *
     * @param gridHeight gridHeight
     */
    public void setGridHeight(String gridHeight) {
        this.gridHeight = gridHeight;
    }

    /**
     * getLimit.
     */
    public int getLimit() {
        return limit;
    }

    /**
     * setLimit.
     *
     * @param limit limit
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * getTotalCount.
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * setTotalCount.
     *
     * @param totalCount totalCount
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * getSelectionModel.
     */
    public String getSelectionModel() {
        return selectionModel;
    }

    /**
     * setSelectionModel.
     *
     * @param selectionModel selectionModel
     */
    public void setSelectionModel(String selectionModel) {
        this.selectionModel = selectionModel;
    }

    /**
     * isVisibleColumnsHeader.
     */
    public boolean isVisibleColumnsHeader() {
        return visibleColumnsHeader;
    }

    /**
     * setVisibleColumnsHeader.
     *
     * @param visibleColumnsHeader visibleColumnsHeader
     */
    public void setVisibleColumnsHeader(boolean visibleColumnsHeader) {
        this.visibleColumnsHeader = visibleColumnsHeader;
    }

    /**
     * isAllowTextSelection.
     */
    public boolean isAllowTextSelection() {
        return allowTextSelection;
    }

    /**
     * setAllowTextSelection.
     *
     * @param allowTextSelection allowTextSelection
     */
    public void setAllowTextSelection(boolean allowTextSelection) {
        this.allowTextSelection = allowTextSelection;
    }

    /**
     * getPrimaryKey.
     */
    public String[] getPrimaryKey() {
        return primaryKey;
    }

    /**
     * setPrimaryKey.
     *
     * @param primaryKey primaryKey
     */
    public void setPrimaryKey(String[] primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * getSummaryRow.
     */
    public Map<String, String> getSummaryRow() {
        return summaryRow;
    }

    /**
     * setSummaryRow.
     *
     * @param summaryRow summaryRow
     */
    public void setSummaryRow(Map<String, String> summaryRow) {
        this.summaryRow = summaryRow;
    }
}
