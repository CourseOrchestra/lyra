package ru.curs.lyra.service.forms;

import ru.curs.celesta.CallContext;
import ru.curs.celesta.syscursors.GrainsCursor;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.annotations.LyraForm;

@LyraForm(gridWidth = "95%", gridHeight = "470px")
public class TestForm extends BasicGridForm<GrainsCursor> {
    public TestForm(CallContext context) {
        super(context);
        createAllBoundFields();
    }

    @Override
    public GrainsCursor getCursor(CallContext context) {
        return new GrainsCursor(context);

    }
}
