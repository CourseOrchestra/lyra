package ru.curs.lyra.service;

import org.springframework.util.ReflectionUtils;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.lyra.dto.FormInstantiationParams;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.annotations.FormParams;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class FormFactory {

    private final Map<String, BasicGridForm<? extends BasicCursor>> forms = new HashMap<>();

    BasicGridForm<? extends BasicCursor> getFormInstance(CallContext callContext,
                                                         FormInstantiationParams parameters,
                                                         LyraService srv) {
        BasicGridForm<?> form = forms.computeIfAbsent(parameters.getDgridId(),
                key -> getBasicGridFormInstance(callContext, parameters, srv));
        form.setCallContext(callContext);
        return setParameters(form, parameters);
    }

    void clearForms() {
        forms.clear();
    }

    private BasicGridForm<? extends BasicCursor> setParameters(
            BasicGridForm<? extends BasicCursor> form, FormInstantiationParams parameters) {
        ReflectionUtils.doWithLocalFields(form.getClass(),
                field -> {
                    if (field.isAnnotationPresent(FormParams.class)
                            && field.getType() == FormInstantiationParams.class) {
                        field.setAccessible(true);
                        ReflectionUtils.setField(field, form, parameters);
                    }
                }
        );
        return form;
    }

    private BasicGridForm<? extends BasicCursor> getBasicGridFormInstance(CallContext callContext,
                                                                          FormInstantiationParams parameters,
                                                                          LyraService srv) {
        try {
            Class<?> clazz = Class.forName(parameters.getFormClass());
            Constructor<?> constructor;
            Object instance;
            try {
                constructor = clazz.getConstructor(CallContext.class, FormInstantiationParams.class);
                instance = constructor.newInstance(callContext, parameters);
            } catch (NoSuchMethodException e) {
                constructor = clazz.getConstructor(CallContext.class);
                instance = constructor.newInstance(callContext);
            }
            BasicGridForm<? extends BasicCursor> form = (BasicGridForm<?>) instance;
            LyraGridScrollBack scrollBack = new LyraGridScrollBack(srv, parameters.getDgridId());
            scrollBack.setBasicGridForm(form);
            form.setChangeNotifier(scrollBack);
            return setParameters(form, parameters);
        } catch (Exception e) {
            throw new CelestaException(e);
        }
    }
}
