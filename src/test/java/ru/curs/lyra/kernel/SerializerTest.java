package ru.curs.lyra.kernel;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.dbutils.Cursor;
import ru.curs.celesta.syscursors.GrainsCursor;
import ru.curs.celesta.syscursors.TablesCursor;
import ru.curs.celestaunit.CelestaUnitExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SerializerTest {

    public static final String SCORE_PATH = "src/test/celestasql";
    @RegisterExtension
    static CelestaUnitExtension ext =
            CelestaUnitExtension.builder()
                    .withScorePath(SCORE_PATH).build();

    @Test
    public void test(CallContext cc) throws UnsupportedEncodingException {

        TablesCursor tt = new TablesCursor(cc);

        Map<String, LyraFormField> metaform = new LinkedHashMap<>();
        LyraFormField lff;
        lff = new LyraFormField("z", new FieldAccessor() {
            @Override
            public Object getValue(Object[] c) {
                return 123;
            }

            @Override
            public void setValue(BasicCursor c, Object newValue) {

            }
        });
        lff.setType(LyraFieldType.INT);
        lff.setRequired(true);
        metaform.put(lff.getName(), lff);

        lff = new LyraFormField("aa", new FieldAccessor() {
            @Override
            public Object getValue(Object[] c) {
                return "русский текст";
            }

            @Override
            public void setValue(BasicCursor c, Object newValue) {

            }
        });
        lff.setType(LyraFieldType.VARCHAR);
        metaform.put(lff.getName(), lff);

        final Date d = new Date();
        lff = new LyraFormField("fe", new FieldAccessor() {
            @Override
            public Object getValue(Object[] c) {
                return d;
            }

            @Override
            public void setValue(BasicCursor c, Object newValue) {

            }
        });
        lff.setType(LyraFieldType.DATETIME);
        metaform.put(lff.getName(), lff);

        lff = new LyraFormField("bs", new FieldAccessor() {
            @Override
            public Object getValue(Object[] c) {
                return true;
            }

            @Override
            public void setValue(BasicCursor c, Object newValue) {

            }
        });
        lff.setType(LyraFieldType.BIT);
        metaform.put(lff.getName(), lff);

        lff = new LyraFormField("we", new FieldAccessor() {
            @Override
            public Object getValue(Object[] c) {
                return null;
            }

            @Override
            public void setValue(BasicCursor c, Object newValue) {

            }
        });
        lff.setType(LyraFieldType.BIT);
        metaform.put(lff.getName(), lff);

        LyraFormData fd = new LyraFormData(tt, metaform, "myform");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        fd.serialize(bos);

        String expected = String.format(
                "<?xml version=\"1.0\" ?><schema recversion=\"0\" formId=\"myform\"><z type=\"INT\" dateFormat=\"dd.MM.yyyy\" decimalSeparator=\",\" groupingSeparator=\" \" required=\"true\">123</z><aa type=\"VARCHAR\" dateFormat=\"dd.MM.yyyy\" decimalSeparator=\",\" groupingSeparator=\" \">русский текст</aa><fe type=\"DATETIME\" dateFormat=\"dd.MM.yyyy\" decimalSeparator=\",\" groupingSeparator=\" \">%s</fe><bs type=\"BIT\" dateFormat=\"dd.MM.yyyy\" decimalSeparator=\",\" groupingSeparator=\" \">true</bs><we type=\"BIT\" dateFormat=\"dd.MM.yyyy\" decimalSeparator=\",\" groupingSeparator=\" \" null=\"true\"></we></schema>",
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(d));

        String actual = bos.toString("utf-8");

        // System.out.println(actual);

        assertEquals(expected, actual);
    }

    @Test
    public void test2(CallContext cc) throws UnsupportedEncodingException {

        GrainsCursor c = new GrainsCursor(cc);

        BasicCardForm<GrainsCursor> bcf = new BasicCardForm<GrainsCursor>(cc) {
            {
                createAllBoundFields();
                createField("aab");
            }

            @Override
            public GrainsCursor getCursor(CallContext context) {
                try {
                    return new GrainsCursor(context);
                } catch (CelestaException e) {
                    return null;
                }
            }

            @Override
            public String getId() {
                return "sdasdf";
            }

            @Override
            public LyraFormField createUnboundField(LyraNamedElementHolder<LyraFormField> meta, String name) {
                try {
                    LyraFormField lff = new LyraFormField(name, new FieldAccessor() {
                        @Override
                        public Object getValue(Object[] c) {
                            return "русский текст";
                        }

                        @Override
                        public void setValue(BasicCursor c, Object newValue) {
                            assertEquals("русский текст", newValue);
                        }
                    });
                    lff.setType(LyraFieldType.VARCHAR);

                    meta.addElement(lff);
                    return lff;
                } catch (CelestaException e) {
                    e.printStackTrace();
                    return null;
                }

            }

            @Override
            public void beforeSending(BasicCursor c) {

            }

            @Override
            public void afterReceiving(Cursor c) {

            }

            @Override
            protected void createAllUnboundFields(LyraNamedElementHolder<LyraFormField> fieldsMeta) {
                // do nothing for this test
            }

            @Override
            public LyraFormProperties getFormProperties() {
                // do nothing for this test
                return null;
            }

        };

        c.get("celesta");
        c.setRecversion(11);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bcf.serialize(c, bos);
        String buf = bos.toString("utf-8");
        // System.out.println(buf);
        assertEquals(8, c.getChecksum().length());

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        GrainsCursor c2 = new GrainsCursor(cc);
        bcf.deserialize(c2, bis);

        assertEquals(c.getRecversion(), c2.getRecversion());
        assertEquals(c.getChecksum(), c2.getChecksum());
        assertEquals(c.getLastmodified().getTime(), c2.getLastmodified().getTime(), 1000);

        bos.reset();
        bcf.serialize(c2, bos);

        assertEquals(buf, bos.toString("utf-8"));
        // System.out.println(buf);

        c2.clear();
        assertNull(c2.getChecksum());
        bis = new ByteArrayInputStream(bos.toByteArray());
        bcf.deserialize(c2, bis);

        assertEquals(c.getChecksum(), c2.getChecksum());
        assertEquals(c.getLastmodified().getTime(), c2.getLastmodified().getTime(), 1000);

    }
}
