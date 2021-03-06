package ru.curs.lyra.kernel;

import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.dbutils.Cursor;

import java.io.*;

/**
 * Base Java class for Lyra card form.
 *
 * @param <T> type of the form's main cursor
 */
public abstract class BasicCardForm<T extends BasicCursor> extends BasicLyraForm<T> {

    private static final String UTF_8 = "utf-8";
    private LyraFormData lfd;

    public BasicCardForm(CallContext context) {
        super(context);
    }

    /**
     * Отыскивает первую запись в наборе записей.
     *
     * @param ctx текущий контекст вызова
     */
    public String findRec(CallContext ctx) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        serialize(rec(ctx), result);
        try {
            return result.toString(UTF_8);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * Отменяет текущие изменения в курсоре и возвращает актуальную информацию
     * из базы данных.
     *
     * @param ctx  текущий контекст вызова
     * @param data сериализованный курсор
     */
    public synchronized String revert(CallContext ctx, String data) {

        Cursor c = getRecCursor(ctx);

        ByteArrayInputStream dataIS;
        try {
            dataIS = new ByteArrayInputStream(data.getBytes(UTF_8));
            deserialize(c, dataIS);
            c.navigate("=<>");
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            serialize(c, result);
            return result.toString(UTF_8);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * Перемещает курсор.
     *
     * @param ctx  текущий контекст вызова
     * @param cmd  Команда перемещения (комбинация знаков &lt;, &gt;, =, +, -, см.
     *             документацию по методу курсора navigate)
     * @param data сериализованный курсор.
     */
    public synchronized String move(CallContext ctx, String cmd, String data) {
        try {
            T rec = rec(ctx);
            if (rec instanceof Cursor) {
                Cursor c = (Cursor) rec;
                ByteArrayInputStream dataIS = new ByteArrayInputStream(data.getBytes(UTF_8));
                deserialize(c, dataIS);
                if (!c.tryUpdate()) {
                    c.insert();
                }
            }
            rec.navigate(cmd);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            serialize(rec(ctx), result);
            return result.toString(UTF_8);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * Инициирует новую запись для вставки в базу данных.
     *
     * @param ctx текущий контекст вызова
     */
    public synchronized String newRec(CallContext ctx) {
        Cursor c = getRecCursor(ctx);
        c.clear();
        c.setRecversion(0);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        serialize(c, result);
        try {
            return result.toString(UTF_8);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * Удаляет текущую запись.
     *
     * @param ctx  текущий контекст вызова
     * @param data сериализованный курсор.
     */
    public synchronized String deleteRec(CallContext ctx, String data) {
        Cursor c = getRecCursor(ctx);

        ByteArrayInputStream dataIS;
        try {
            dataIS = new ByteArrayInputStream(data.getBytes(UTF_8));

            deserialize(c, dataIS);

            c.delete();
            if (!c.navigate(">+")) {
                c.clear();
                c.setRecversion(0);
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            serialize(c, result);
            return result.toString(UTF_8);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private Cursor getRecCursor(CallContext ctx) {
        BasicCursor rec = rec(ctx);
        if (rec instanceof Cursor) {
            return (Cursor) rec;
        } else {
            throw new CelestaException("Cursor %s is not modifiable.", rec.meta().getName());
        }
    }

    void serialize(BasicCursor c, OutputStream result) {
        beforeSending(c);
        lfd = new LyraFormData(c, getFieldsMeta(), getId());
        lfd.serialize(result);
    }

    void deserialize(Cursor c, InputStream dataIS) {
        lfd = new LyraFormData(dataIS);
        lfd.populateFields(c, getFieldsMeta());
        afterReceiving(c);
    }

    public abstract void afterReceiving(Cursor c);
}
