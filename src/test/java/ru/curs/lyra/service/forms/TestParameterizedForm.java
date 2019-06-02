package ru.curs.lyra.service.forms;

import foo.FooCursor;
import ru.curs.celesta.CallContext;
import ru.curs.lyra.dto.FormInstantiationParams;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.GridRefinementHandler;
import ru.curs.lyra.kernel.annotations.FormParams;
import ru.curs.lyra.kernel.annotations.LyraForm;

@LyraForm
public class TestParameterizedForm extends BasicGridForm<FooCursor> {

    @FormParams
    private FormInstantiationParams params;

    public FormInstantiationParams getConstructorParams() {
        return constructorParams;
    }

    private FormInstantiationParams constructorParams;

    public TestParameterizedForm(CallContext context, GridRefinementHandler handler, FormInstantiationParams constructorParams) {
        super(context, handler);
        this.constructorParams = constructorParams;
        createAllBoundFields();
    }

    public FormInstantiationParams getParams() {
        return params;
    }

    @Override
    public FooCursor getCursor(CallContext context) {
        return new FooCursor(context);
    }
}
