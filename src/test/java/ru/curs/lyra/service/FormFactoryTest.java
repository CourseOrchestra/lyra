package ru.curs.lyra.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celestaunit.CelestaUnitExtension;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.grid.GridDriver;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static ru.curs.lyra.service.LyraServiceTest.SCORE_PATH;


class FormFactoryTest {
    @RegisterExtension
    static CelestaUnitExtension ext =
            CelestaUnitExtension.builder()
                    .withScorePath(SCORE_PATH).build();

    FormFactory formFactory = new FormFactory();
    LyraService srv = new LyraService(null);

    @Test
    void getFormInstanceReturnsSameInstanceIfCalledTwice(CallContext ctx) {

        FormInstantiationParameters ip = new FormInstantiationParameters("ru.curs.lyra.service.TestForm", "foo");
        BasicGridForm<? extends BasicCursor> fooForm = formFactory.getFormInstance(ctx, ip, srv);
        assertEquals(GridDriver.DEFAULT_SMALL_SCROLL, fooForm.getMaxExactScrollValue());
        assertTrue(fooForm instanceof TestForm);
        BasicGridForm<? extends BasicCursor> fooForm2 = formFactory.getFormInstance(ctx, ip, srv);
        assertSame(fooForm, fooForm2);
        ip = new FormInstantiationParameters("ru.curs.lyra.service.TestForm", "baz");
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

        //System.out.println(foo.getMessage());
    }

    @Test
    void parameterizedFormInstantiation(CallContext ctx){
        Map<String, String> params = new HashMap<>();
        params.put("part1", "part1");
        params.put("filter", "filter conditions");
        FormInstantiationParameters parameters = new FormInstantiationParameters(
                "ru.curs.lyra.service.TestParameterizedForm",
                "foo", params);
        TestParameterizedForm fooForm = (TestParameterizedForm) formFactory.getFormInstance(ctx, parameters, srv);

        assertSame(parameters, fooForm.getParams());
        assertSame(parameters, fooForm.getConstructorParams());
    }
}