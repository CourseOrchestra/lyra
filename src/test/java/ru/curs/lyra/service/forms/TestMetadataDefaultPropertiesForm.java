package ru.curs.lyra.service.forms;

import foo.FooCursor;
import ru.curs.celesta.CallContext;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.GridRefinementHandler;
import ru.curs.lyra.kernel.annotations.FormField;
import ru.curs.lyra.kernel.annotations.LyraForm;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@LyraForm
public class TestMetadataDefaultPropertiesForm extends BasicGridForm<FooCursor> {
    public TestMetadataDefaultPropertiesForm(CallContext context, GridRefinementHandler handler) {
        super(context, handler);
        createAllBoundFields();

        createField("unboundField1");
        createField("unboundField2");
    }

    @Override
    public FooCursor getCursor(CallContext context) {
        return new FooCursor(context);
    }

    @FormField
    public double getUnboundField1(CallContext ctx) {
        return rec(ctx).getId() + 0.12;
    }

    @FormField
    public Date getUnboundField2(CallContext ctx) {
        return Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
    }


}
