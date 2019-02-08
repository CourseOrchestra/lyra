package ru.curs.lyra.service;

//TODO: make immutable with builder
public class DataRetrievalParams {
    private int offset;
    private int limit;
    private int dgridOldPosition;
    private boolean sortingOrFilteringChanged;
    private boolean firstLoading;
    private String refreshId;
    private int totalCount;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getDgridOldPosition() {
        return dgridOldPosition;
    }

    public void setDgridOldPosition(int dgridOldPosition) {
        this.dgridOldPosition = dgridOldPosition;
    }

    public boolean isSortingOrFilteringChanged() {
        return sortingOrFilteringChanged;
    }

    public void setSortingOrFilteringChanged(boolean sortingOrFilteringChanged) {
        this.sortingOrFilteringChanged = sortingOrFilteringChanged;
    }

    public boolean isFirstLoading() {
        return firstLoading;
    }

    public void setFirstLoading(boolean firstLoading) {
        this.firstLoading = firstLoading;
    }

    public String getRefreshId() {
        return refreshId;
    }

    public void setRefreshId(String refreshId) {
        this.refreshId = refreshId;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
