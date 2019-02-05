package ru.curs.lyra.dto;

public class ScrollBackParams {

    private String dgridId;
    private int position;


    public ScrollBackParams(String dgridId, int position) {
        this.dgridId = dgridId;
        this.position = position;
    }

    @SuppressWarnings("unused")
    public String getDgridId() {
        return dgridId;
    }

    @SuppressWarnings("unused")
    public int getPosition() {
        return position;
    }


}
