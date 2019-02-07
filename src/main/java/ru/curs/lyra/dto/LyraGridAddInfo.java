package ru.curs.lyra.dto;


import ru.curs.lyra.service.GridToExcelExportType;

public class LyraGridAddInfo {

    private int lyraOldPosition = 0;
    private int dgridOldTotalCount = 0;

    private GridToExcelExportType excelExportType = null;

    private boolean needRecreateWebsocket = false;

    public int getLyraOldPosition() {
        return lyraOldPosition;
    }

    public void setLyraOldPosition(final int aLyraOldPosition) {
        lyraOldPosition = aLyraOldPosition;
    }

    public int getDgridOldTotalCount() {
        return dgridOldTotalCount;
    }

    public void setDgridOldTotalCount(final int aDgridOldTotalCount) {
        dgridOldTotalCount = aDgridOldTotalCount;
    }

    public GridToExcelExportType getExcelExportType() {
        return excelExportType;
    }

    public void setExcelExportType(final GridToExcelExportType excelExportType) {
        this.excelExportType = excelExportType;
    }

    public boolean isNeedRecreateWebsocket() {
        return needRecreateWebsocket;
    }

    public void setNeedRecreateWebsocket(final boolean needRecreateWebsocket) {
        this.needRecreateWebsocket = needRecreateWebsocket;
    }

}
