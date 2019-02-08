package ru.curs.lyra.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celestaunit.CelestaUnitExtension;
import ru.curs.lyra.kernel.BasicGridForm;

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
    void getDgridId() {
        assertEquals("foo.bar", FormFactory.getDgridId("foo", "bar"));
    }

    @Test
    void getFormInstanceReturnsSameInstanceIfCalledTwice(CallContext ctx) {
        BasicGridForm<? extends BasicCursor> fooForm = formFactory.getFormInstance(ctx, "ru.curs.lyra.service.TestForm", "foo", srv);
        assertTrue(fooForm instanceof TestForm);
        BasicGridForm<? extends BasicCursor> fooForm2 = formFactory.getFormInstance(ctx, "ru.curs.lyra.service.TestForm", "foo", srv);
        assertSame(fooForm, fooForm2);

        BasicGridForm<? extends BasicCursor> fooForm3 = formFactory.getFormInstance(ctx, "ru.curs.lyra.service.TestForm", "baz", srv);
        assertNotSame(fooForm, fooForm3);
    }

    @Test
    void getFormInstanceThrowsCelestaExceptionOnUnknownFormClass(CallContext ctx) {
        assertThrows(CelestaException.class,
                () ->
                        formFactory.getFormInstance(ctx, "ru.curs.lyra.service.TestForm11111", "foo", srv));

        //System.out.println(foo.getMessage());
    }
}