package ru.curs.lyra.kernel.forms;

import foo.FooCursor;
import ru.curs.celesta.CallContext;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.GridRefinementHandler;
import ru.curs.lyra.kernel.annotations.FormField;

public class TestFormWithDuplicatedFields extends BasicGridForm<FooCursor> {

    public TestFormWithDuplicatedFields(CallContext context, GridRefinementHandler handler) {
        super(context, handler);
    }

    @Override
    public FooCursor getCursor(CallContext context) {
        return new FooCursor(context);
    }

    @FormField
    String getFoo() {
        return "";
    }

    @FormField
    String getFoo(CallContext ctx){
        return "";
    }
}
