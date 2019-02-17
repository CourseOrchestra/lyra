package ru.curs.lyra.kernel.forms;

import foo.FooCursor;
import ru.curs.celesta.CallContext;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.annotations.FormField;

public class TestFormWithUnboundFields extends BasicGridForm<FooCursor> {

    public static final int RETURN_VALUE = 100;

    public static final String RETURN_VALUE2 = "text return value";

    public TestFormWithUnboundFields(CallContext context) {
        super(context);
    }

    @Override
    public FooCursor getCursor(CallContext context) {
        return new FooCursor(context);
    }

    @FormField
    int getSomething(CallContext ctx) {
        return RETURN_VALUE;
    }

    @FormField(caption = "test caption")
    String getNoContext() {
        return RETURN_VALUE2;
    }

    @FormField
    int getException(){
        throw new IllegalStateException("test message");
    }

}
