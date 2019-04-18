package ru.curs.lyra.dto;

import java.util.List;
import java.util.Map;

public final class DataResult {

    Map<String, Labels> objAddData;
    private List<Map<String, Object>> data;

    public Map<String, Labels> getObjAddData() {
        return objAddData;
    }

    public void setObjAddData(Map<String, Labels> objAddData) {
        this.objAddData = objAddData;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}
