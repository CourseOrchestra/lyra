package ru.curs.lyra.service;

import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.lyra.kernel.BasicGridForm;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class FormFactory {

    private final Map<String, BasicGridForm<? extends BasicCursor>> forms = new HashMap<>();

    public static String getDgridId(String formClass, String instanceId) {
        return formClass + "." + instanceId;
    }

    public  BasicGridForm<? extends BasicCursor> getFormInstance(CallContext callContext,
                                                                 String formClass,
                                                                 String instanceId,
                                                                 LyraService srv) {
        String dgridId = getDgridId(formClass, instanceId);
        BasicGridForm<?> form = forms.computeIfAbsent(dgridId, key -> getBasicGridFormInstance(callContext, formClass, dgridId, srv));
        form.setCallContext(callContext);
        return form;

    }

    private BasicGridForm<? extends BasicCursor> getBasicGridFormInstance(CallContext callContext,
                                                                              String formClass,
                                                                              String dgridId,
                                                                              LyraService srv) throws CelestaException {
        try {

            Class<?> clazz = Class.forName(formClass);
            Constructor<?> constructor = clazz.getConstructor(CallContext.class);
            Object instance = constructor.newInstance(callContext);
            BasicGridForm<?> form = (BasicGridForm<?>) instance;
            final int maxExactScrollValue = 120;
            form.setMaxExactScrollValue(maxExactScrollValue);
            LyraGridScrollBack scrollBack = new LyraGridScrollBack(srv, dgridId);
            scrollBack.setBasicGridForm(form);
            form.setChangeNotifier(scrollBack);
            forms.put(dgridId, form);
            return form;
        } catch (Exception e) {
            throw new CelestaException(e);
        }
    }
}
