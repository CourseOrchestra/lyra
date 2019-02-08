package ru.curs.lyra.dto;


public class LyraGridAddInfo {

    private int lyraOldPosition = 0;
    private int dgridOldTotalCount = 0;

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

    public boolean isNeedRecreateWebsocket() {
        return needRecreateWebsocket;
    }

    public void setNeedRecreateWebsocket(final boolean needRecreateWebsocket) {
        this.needRecreateWebsocket = needRecreateWebsocket;
    }

}
