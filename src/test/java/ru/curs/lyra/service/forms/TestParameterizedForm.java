package ru.curs.lyra.service.forms;

import foo.FooCursor;
import ru.curs.celesta.CallContext;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.annotations.FormParams;
import ru.curs.lyra.kernel.annotations.LyraForm;
import ru.curs.lyra.service.FormInstantiationParameters;

@LyraForm
public class TestParameterizedForm extends BasicGridForm<FooCursor> {

    @FormParams
    private FormInstantiationParameters params;

    public FormInstantiationParameters getConstructorParams() {
        return constructorParams;
    }

    private FormInstantiationParameters constructorParams;

    public TestParameterizedForm(CallContext context, FormInstantiationParameters constructorParams) {
        super(context);
        this.constructorParams = constructorParams;
        createAllBoundFields();
    }

    public FormInstantiationParameters getParams() {
        return params;
    }

    @Override
    public FooCursor getCursor(CallContext context) {
        return new FooCursor(context);
    }
}
