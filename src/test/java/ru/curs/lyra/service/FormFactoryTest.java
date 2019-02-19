package ru.curs.lyra.service;

import org.junit.jupiter.api.Test;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celestaunit.CelestaTest;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.grid.GridDriver;
import ru.curs.lyra.service.forms.TestForm;
import ru.curs.lyra.service.forms.TestParameterizedForm;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@CelestaTest
class FormFactoryTest {
    FormFactory formFactory = new FormFactory();
    LyraService srv = new LyraService(null);

    @Test
    void getFormInstanceReturnsSameInstanceIfCalledTwice(CallContext ctx) {

        FormInstantiationParameters ip = new FormInstantiationParameters("ru.curs.lyra.service.forms.TestForm", "foo");
        BasicGridForm<? extends BasicCursor> fooForm = formFactory.getFormInstance(ctx, ip, srv);
        assertEquals(GridDriver.DEFAULT_SMALL_SCROLL, fooForm.getMaxExactScrollValue());
        assertTrue(fooForm instanceof TestForm);
        BasicGridForm<? extends BasicCursor> fooForm2 = formFactory.getFormInstance(ctx, ip, srv);
        assertSame(fooForm, fooForm2);
        ip = new FormInstantiationParameters("ru.curs.lyra.service.forms.TestForm", "baz");
        BasicGridForm<? extends BasicCursor> fooForm3 = formFactory.getFormInstance(ctx, ip, srv);
        assertNotSame(fooForm, fooForm3);
    }

    @Test
    void getFormInstanceThrowsCelestaExceptionOnUnknownFormClass(CallContext ctx) {
        FormInstantiationParameters ip = new FormInstantiationParameters("ru.curs.lyra.service.TestForm1111",
                "foo");
        assertThrows(CelestaException.class,
                () ->
                        formFactory.getFormInstance(ctx, ip, srv));
    }

    @Test
    void parameterizedFormInstantiation(CallContext ctx){
        Map<String, String> params = new HashMap<>();
        params.put("part1", "part1");
        params.put("filter", "filter conditions");
        FormInstantiationParameters parameters = new FormInstantiationParameters(
                "ru.curs.lyra.service.forms.TestParameterizedForm",
                "foo", params);
        TestParameterizedForm fooForm = (TestParameterizedForm) formFactory.getFormInstance(ctx, parameters, srv);

        assertSame(parameters, fooForm.getParams());
        assertSame(parameters, fooForm.getConstructorParams());
    }
}
