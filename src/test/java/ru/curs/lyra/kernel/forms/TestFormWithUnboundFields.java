package ru.curs.lyra.kernel.forms;

import foo.FooCursor;
import ru.curs.celesta.CallContext;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.GridRefinementHandler;
import ru.curs.lyra.kernel.annotations.FormField;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestFormWithUnboundFields extends BasicGridForm<FooCursor> {

    public static final int RETURN_VALUE = 100;

    public static final String RETURN_VALUE2 = "text return value";

    public TestFormWithUnboundFields(CallContext context, GridRefinementHandler notifier) {
        super(context, notifier);
        assertFalse(context.isClosed());
    }

    @Override
    public FooCursor getCursor(CallContext context) {
        return new FooCursor(context);
    }

    @FormField
    int getSomething(CallContext ctx) {
        assertFalse(ctx.isClosed());
        return RETURN_VALUE;
    }

    @FormField(caption = "test caption")
    String getNoContext() {
        return RETURN_VALUE2;
    }

    @FormField
    Date getException() {
        throw new IllegalStateException("test message");
    }

    @FormField
    boolean isBoolean() {
        return true;
    }

    int notAGetter(CallContext ctx) {
        return 0;
    }
}
