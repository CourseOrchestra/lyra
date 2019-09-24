package ru.curs.lyra.dto;

import java.util.ArrayList;
import java.util.List;

public final class MetaDataResult {

    private Common common;
    private List<Column> columns = new ArrayList<>();

    public Common getCommon() {
        return common;
    }

    public void setCommon(Common common) {
        this.common = common;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
}
