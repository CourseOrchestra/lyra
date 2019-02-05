package ru.curs.lyra.dto;

public class DataParams {


    private String context;
    private int offset;
    private int limit;
    private int dgridOldPosition;
    private boolean sortingOrFilteringChanged;
    private boolean firstLoading;
    private String refreshId;
    private String formClass;
    private String instanceId;

    private int totalCount;


    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

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

    public String getFormClass() {
        return formClass;
    }

    public void setFormClass(String formClass) {
        this.formClass = formClass;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
