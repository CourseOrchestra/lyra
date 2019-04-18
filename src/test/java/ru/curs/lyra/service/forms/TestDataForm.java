package ru.curs.lyra.service.forms;

import foo.FooCursor;
import ru.curs.celesta.CallContext;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.annotations.LyraForm;

@LyraForm(gridHeader = "<h2>Header2</h2>",
        gridFooter = "<h2>Footer2</h2>")
public class TestDataForm extends BasicGridForm<FooCursor> {
    public TestDataForm(CallContext context) {
        super(context);
        createAllBoundFields();
    }

    @Override
    public FooCursor getCursor(CallContext context) {
        return new FooCursor(context);
    }


}
