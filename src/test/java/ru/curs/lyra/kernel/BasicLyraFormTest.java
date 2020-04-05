package ru.curs.lyra.kernel;

import foo.FooCursor;
import org.junit.jupiter.api.Test;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.score.Table;
import ru.curs.celestaunit.CelestaTest;
import ru.curs.lyra.kernel.forms.TestFormWithDuplicatedFields;
import ru.curs.lyra.kernel.forms.TestFormWithUnboundFields;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@CelestaTest
class BasicLyraFormTest {
    @Test
    void createUnboundField(CallContext ctx) {
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx, null);
        LyraFormField newField = testForm.createField("something");
        assertEquals("something", newField.getName());
        assertEquals(LyraFieldType.INT, newField.getType());
        assertEquals(TestFormWithUnboundFields.RETURN_VALUE, newField.getAccessor().getValue(testForm.rec(ctx)));

        assertEquals(1, testForm.getFieldsMeta().size());
        assertSame(newField, testForm.getFieldsMeta().get("something"));
    }

    @Test
    void createNonExistentUnboundField(CallContext ctx) {
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx, null);
        assertTrue(
                assertThrows(CelestaException.class,
                        () -> testForm.createField("nonexistent")).getMessage().contains("nonexistent")
        );
    }

    @Test
    void createUnboundFieldWithNoContextParameter(CallContext ctx) {
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx, null);
        LyraFormField newField = testForm.createField("noContext");
        assertEquals("noContext", newField.getName());
        assertEquals(TestFormWithUnboundFields.RETURN_VALUE2, newField.getAccessor().getValue(testForm.rec(ctx)));
        assertEquals("test caption", newField.getCaption());
    }

    @Test
    void createAllUnboundFields(CallContext ctx) {
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx, null);
        testForm.createAllUnboundFields();

        assertEquals(4, testForm.getFieldsMeta().size());
        LyraFormField newField = testForm.getFieldsMeta().get("something");
        assertEquals(LyraFieldType.INT, newField.getType());

        newField = testForm.getFieldsMeta().get("noContext");
        assertEquals(LyraFieldType.VARCHAR, newField.getType());
        assertEquals("test caption", newField.getCaption());

        newField = testForm.getFieldsMeta().get("exception");
        assertEquals(LyraFieldType.DATETIME, newField.getType());

        newField = testForm.getFieldsMeta().get("boolean");
        assertEquals(LyraFieldType.BIT, newField.getType());
    }

    @Test
    void createBoundField(CallContext ctx) {
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx, null);
        LyraFormField newField = testForm.createField("name");
        assertEquals(LyraFieldType.VARCHAR, newField.getType());
        assertEquals("name field caption", newField.getCaption());
    }

    @Test
    void createAllBoundFields(CallContext ctx) {
        Table meta = new FooCursor(ctx).meta();
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx, null);
        testForm.createAllBoundFields();
        Map<String, LyraFormField> fieldsMeta = testForm.getFieldsMeta();
        assertTrue(fieldsMeta.size() > 0);
        assertEquals(meta.getColumns().size(), fieldsMeta.size());
        meta.getColumns().keySet().forEach(s -> {
            LyraFormField field = fieldsMeta.get(s);
            assertEquals(field.getName(), s);
        });


    }

    @Test
    void createDuplicatedUnboundFields(CallContext ctx) {
        TestFormWithDuplicatedFields form = new TestFormWithDuplicatedFields(ctx, null);
        assertTrue(assertThrows(CelestaException.class, form::createAllUnboundFields).getMessage().contains("foo"));
    }

    @Test
    void catchGetterException(CallContext ctx) {
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx, null);
        LyraFormField exception = testForm.createField("exception");
        assertTrue(assertThrows(CelestaException.class,
                () -> exception.getAccessor().getValue(testForm.rec(ctx))).getMessage().contains("test message"));
    }
}
