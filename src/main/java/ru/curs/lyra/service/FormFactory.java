package ru.curs.lyra.service;

import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.lyra.kernel.BasicGridForm;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FormFactory {

    private final Map<String, BasicGridForm<? extends BasicCursor>> forms = new HashMap<>();

    public BasicGridForm<? extends BasicCursor> getFormInstance(CallContext callContext,
                                                                FormInstantiationParameters parameters,
                                                                LyraService srv) {
        BasicGridForm<?> form = forms.computeIfAbsent(parameters.getDgridId(),
                key -> getBasicGridFormInstance(callContext, parameters, srv));
        form.setCallContext(callContext);
        return form;

    }

    private BasicGridForm<? extends BasicCursor> getBasicGridFormInstance(CallContext callContext,
                                                                          FormInstantiationParameters parameters,
                                                                          LyraService srv) {
        try {
            Class<?> clazz = Class.forName(parameters.getFormClass());
            Constructor<?> constructor;
            Object instance;
            try {
                constructor = clazz.getConstructor(CallContext.class, Map.class);
                instance = constructor.newInstance(callContext, parameters.getClientParams());
            } catch (NoSuchMethodException e) {
                constructor = clazz.getConstructor(CallContext.class);
                instance = constructor.newInstance(callContext);
            }
            BasicGridForm<?> form = (BasicGridForm<?>) instance;
            final int maxExactScrollValue = 120;
            form.setMaxExactScrollValue(maxExactScrollValue);
            LyraGridScrollBack scrollBack = new LyraGridScrollBack(srv, parameters.getDgridId());
            scrollBack.setBasicGridForm(form);
            form.setChangeNotifier(scrollBack);
            forms.put(parameters.getDgridId(), form);
            return form;
        } catch (Exception e) {
            throw new CelestaException(e);
        }
    }
}
