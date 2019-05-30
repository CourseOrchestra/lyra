package ru.curs.lyra.service.forms;

import foo.FooCursor;
import ru.curs.celesta.CallContext;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.annotations.FormField;
import ru.curs.lyra.kernel.annotations.LyraForm;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@LyraForm(gridWidth = "500px", gridHeight = "470px",
        gridHeader = "<h1>Header</h1>",
        gridFooter = "<h1>Footer</h1>",
        visibleColumnsHeader = false,
        allowTextSelection = false)
public class TestMetadataForm extends BasicGridForm<FooCursor> {
    public TestMetadataForm(CallContext context) {
        super(context);
        createAllBoundFields();

        getFieldsMeta().get("id").setCssClassName("className1");
        getFieldsMeta().get("id").setCssStyle("width:100px;text-align:right;");

        getFieldsMeta().get("name").setVisible(false);
        getFieldsMeta().get("name").setCssClassName("className2");
        getFieldsMeta().get("name").setCssStyle("width:300px;text-align:left;");

        createField("unboundField1");
        createField("unboundField2");

    }

    @Override
    public FooCursor getCursor(CallContext context) {
        return new FooCursor(context);
    }

    @FormField(caption = "REAL",
            visible = true,
            cssStyle = "white-space:nowrap;width:100px;text-align:right;",
            sortable = false,
            scale = 1)
    public double getUnboundField1(CallContext ctx) {
        return rec(ctx).getId() + 0.12;
    }

    @FormField(caption = "DATETIME",
            visible = false,
            cssStyle = "white-space:nowrap;width:70px;text-align:center;",
            dateFormat = "yyyy.MM.dd G 'at' HH:mm:ss z")
    public Date getUnboundField2(CallContext ctx) {
        return Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public int getGridHeight() {
        return 10;
    }

    @Override
    public Map<String, String> getSummaryRow() {
        Map<String, String> sum = new HashMap<>();
        sum.put("id", "ID");
        sum.put("name", "NAME");
        return sum;
    }

}
