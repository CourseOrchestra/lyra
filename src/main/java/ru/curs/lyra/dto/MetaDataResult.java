package ru.curs.lyra.dto;

import java.util.HashMap;
import java.util.Map;

public class MetaDataResult {

    private Common common;
    private Map<String, Column> columns = new HashMap<>();

    public Common getCommon() {
        return common;
    }

    public void setCommon(Common common) {
        this.common = common;
    }

    public Map<String, Column> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, Column> columns) {
        this.columns = columns;
    }
}
