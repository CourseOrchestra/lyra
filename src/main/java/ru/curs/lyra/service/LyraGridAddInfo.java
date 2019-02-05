package ru.curs.lyra.service;


class LyraGridAddInfo {

    private int lyraOldPosition = 0;
    private int dgridOldTotalCount = 0;

    private GridToExcelExportType excelExportType = null;

    private boolean needRecreateWebsocket = false;

    int getLyraOldPosition() {
        return lyraOldPosition;
    }

    void setLyraOldPosition(final int aLyraOldPosition) {
        lyraOldPosition = aLyraOldPosition;
    }

    int getDgridOldTotalCount() {
        return dgridOldTotalCount;
    }

    void setDgridOldTotalCount(final int aDgridOldTotalCount) {
        dgridOldTotalCount = aDgridOldTotalCount;
    }

    GridToExcelExportType getExcelExportType() {
        return excelExportType;
    }

    void setExcelExportType(final GridToExcelExportType excelExportType) {
        this.excelExportType = excelExportType;
    }

    boolean isNeedRecreateWebsocket() {
        return needRecreateWebsocket;
    }

    void setNeedRecreateWebsocket(final boolean needRecreateWebsocket) {
        this.needRecreateWebsocket = needRecreateWebsocket;
    }

}
