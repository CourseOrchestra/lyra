package ru.curs.lyra.service;

import foo.FooCursor;
import ru.curs.celesta.CallContext;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.annotations.LyraForm;

import java.util.Map;

@LyraForm
public class TestParameterizedForm extends BasicGridForm<FooCursor> {


    private final Map<String, String> params;

    public TestParameterizedForm(CallContext context, Map<String, String> params) {
        super(context);
        this.params = params;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public FooCursor getCursor(CallContext context) {
        return new FooCursor(context);
    }
}
