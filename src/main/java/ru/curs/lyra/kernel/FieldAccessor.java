package ru.curs.lyra.kernel;

import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstract Lyra form field getter/setter.
 */
public interface FieldAccessor {
    /**
     * Get field's value.
     *
     * @param c Cursor values (ignored for unbound field).
     */
    Object getValue(Object[] c);

    /**
     * Set field's value.
     *
     * @param c        Cursor (ignored for unbound field).
     * @param newValue New field's value.
     */
    void setValue(BasicCursor c, Object newValue);

}

/**
 * FieldAccessor factory: instantiates an appropriate FieldAccessor for bound
 * field given a ColumnMeta.
 */
final class FieldAccessorFactory {

    /**
     * Base class for bound field accessor implementations.
     */
    private abstract static class BasicBoundFieldAccessor implements FieldAccessor {
        private final int index;
        private final String name;

        BasicBoundFieldAccessor(int index, String name) {
            this.index = index;
            this.name = name;
        }

        @Override
        public final Object getValue(Object[] c) {
            return c[index];
        }

        @Override
        public final void setValue(BasicCursor c, Object newValue) {
            if (newValue == null) {
                c.setValue(name, null);
            } else {
                String buf = newValue.toString();
                setValue(c, newValue, buf);
            }
        }

        final String name() {
            return name;
        }

        abstract void setValue(BasicCursor c, Object newValue, String buf);
    }

    private FieldAccessorFactory() {

    }

    static FieldAccessor create(int index, String name, LyraFieldType lft) {

        switch (lft) {
            case DATETIME:
                return new BasicBoundFieldAccessor(index, name) {
                    private SimpleDateFormat sdf;

                    @Override
                    public void setValue(BasicCursor c, Object val, String buf) {
                        if (val instanceof Date) {
                            c.setValue(name(), val);
                        } else {
                            if (sdf == null) {
                                sdf = new SimpleDateFormat(LyraFieldValue.XML_DATE_FORMAT);
                            }
                            Date d;
                            try {
                                d = sdf.parse(buf);
                            } catch (java.text.ParseException e) {
                                d = null;
                            }
                            c.setValue(name(), d);
                        }
                    }
                };

            case BIT:
                return new BasicBoundFieldAccessor(index, name) {
                    @Override
                    public void setValue(BasicCursor c, Object val, String buf) {
                        c.setValue(name(), Boolean.valueOf(buf));
                    }
                };
            case INT:
                return new BasicBoundFieldAccessor(index, name) {
                    @Override
                    public void setValue(BasicCursor c, Object val, String buf) {
                        c.setValue(name(), Integer.valueOf(buf));
                    }
                };

            case REAL:
                return new BasicBoundFieldAccessor(index, name) {
                    @Override
                    public void setValue(BasicCursor c, Object val, String buf) {
                        c.setValue(name(), Double.valueOf(buf));
                    }
                };

            default:
                return new BasicBoundFieldAccessor(index, name) {
                    @Override
                    public void setValue(BasicCursor c, Object val, String buf) {
                        c.setValue(name(), buf);
                    }
                };

        }

    }
}

final class UnboundFieldAccessor implements FieldAccessor {

    private Method getter;
    private Method setter;
    private LyraFieldType lyraFieldType;
    BasicLyraForm basicLyraForm;

    void setLyraFieldType(LyraFieldType lyraFieldType) {
        this.lyraFieldType = lyraFieldType;
    }

    UnboundFieldAccessor(Method getter, Method setter, BasicLyraForm basicLyraForm) {
        this.getter = getter;
        this.setter = setter;
        this.basicLyraForm = basicLyraForm;
    }

    @Override
    public Object getValue(Object[] c) {

        Object value;
        try {
            getter.setAccessible(true);
            if (getter.getParameterCount() == 0){
                value = getter.invoke(basicLyraForm);
            } else {
                value = getter.invoke(basicLyraForm, basicLyraForm.getContext());
            }
        } catch (Throwable e) {
            throw new CelestaException(
                    "Error %s while getting unbound field value: %s. See logs for details.",
                    e.getClass().getName(), e.getMessage());
        }

        switch (lyraFieldType) {
            case BIT:
                return (Boolean) value;
            case DATETIME:
                return (Date) value;
            case REAL:
                return (Double) value;
            case INT:
                return (Integer) value;
            case VARCHAR:
                return (String) value;
            default:
                return null;
        }

    }

    @Override
    public void setValue(BasicCursor c, Object newValue) {
        try {
            setter.invoke(basicLyraForm, newValue);
        } catch (Throwable e) {
            throw new CelestaException(
                    "Error %s while getting unbound field value: %s. See logs for details.",
                    e.getClass().getName(), e.getMessage());
        }
    }
}
