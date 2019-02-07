package ru.curs.lyra.service;

import ru.curs.celesta.CallContext;
import ru.curs.celesta.syscursors.GrainsCursor;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.annotations.LyraForm;

@LyraForm(gridWidth = "95%", gridHeight = "470px",
        gridHeader = "<h5>Это хедер лира-грида</h5>",
        gridFooter = "<h5>Это футер лира-грида</h5>")
public class TestForm extends BasicGridForm<GrainsCursor> {
    public TestForm(CallContext context) {
        super(context);
    }

    @Override
    public GrainsCursor getCursor(CallContext context) {
        return new GrainsCursor(context);

    }
}
