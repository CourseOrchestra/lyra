package ru.curs.lyra.kernel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celestaunit.CelestaUnitExtension;
import ru.curs.lyra.kernel.forms.TestFormWithUnboundFields;

import static org.junit.jupiter.api.Assertions.*;

class BasicLyraFormTest {
    public static final String SCORE_PATH = "src/test/resources/score";
    @RegisterExtension
    static CelestaUnitExtension ext =
            CelestaUnitExtension.builder()
                    .withScorePath(SCORE_PATH).build();

    @Test
    void createUnboundField(CallContext ctx) {
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx);
        LyraFormField newField = testForm.createField("something");
        assertEquals("something", newField.getName());
        assertEquals(LyraFieldType.INT, newField.getType());
        assertEquals(TestFormWithUnboundFields.RETURN_VALUE, newField.getAccessor().getValue(new Object[0]));

        assertEquals(1, testForm.getFieldsMeta().size());
        assertSame(newField, testForm.getFieldsMeta().get("something"));
    }

    @Test
    void createNonExistentUnboundField(CallContext ctx) {
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx);
        assertTrue(
                assertThrows(CelestaException.class,
                        () -> testForm.createField("nonexistent")).getMessage().contains("nonexistent")
        );
    }

    @Test
    void createUnboundFieldWithNoContextParameter(CallContext ctx) {
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx);
        LyraFormField newField = testForm.createField("noContext");
        assertEquals("noContext", newField.getName());
        assertEquals(TestFormWithUnboundFields.RETURN_VALUE2, newField.getAccessor().getValue(new Object[0]));
        assertEquals("test caption", newField.getCaption());
    }

    @Test
    void createAllUnboundFields(CallContext ctx) {
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx);
        testForm.createAllUnboundFields();

        assertEquals(2, testForm.getFieldsMeta().size());
        LyraFormField newField = testForm.getFieldsMeta().get("something");
        assertEquals(LyraFieldType.INT, newField.getType());

        newField = testForm.getFieldsMeta().get("noContext");
        assertEquals(LyraFieldType.VARCHAR, newField.getType());
        assertEquals("test caption", newField.getCaption());
    }

    @Test
    void createBoundField(CallContext ctx){
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx);
        LyraFormField newField = testForm.createField("name");
        assertEquals(LyraFieldType.VARCHAR, newField.getType());
        assertEquals("name field caption", newField.getCaption());


    }
}