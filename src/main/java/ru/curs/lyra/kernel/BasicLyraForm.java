package ru.curs.lyra.kernel;

import org.json.JSONException;
import org.json.JSONObject;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.dbutils.Cursor;
import ru.curs.celesta.score.*;
import ru.curs.lyra.kernel.annotations.FormField;
import ru.curs.lyra.kernel.annotations.LyraForm;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import static ru.curs.lyra.kernel.LyraFormField.*;

/**
 * Base Java class for Lyra forms. Two classes inherited from this one are
 * BasicCardForm and BasicGridForm.
 */
public abstract class BasicLyraForm<T extends BasicCursor> {

    public static final String PROPERTIES = "recordProperties";

    private final DataGrainElement meta;
    private final LyraNamedElementHolder<LyraFormField> fieldsMeta = new LyraNamedElementHolder<LyraFormField>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected String getErrorMsg(String name) {
            return String.format("Field '%s' defined more than once in a form.", name);
        }
    };

    private T rec;
    private CallContext context;

    LyraFormProperties lyraFormProperties = new LyraFormProperties();

    public BasicLyraForm(CallContext context) {

        LyraForm lyraForm = getClass().getAnnotation(LyraForm.class);
        if (lyraForm != null) {
            lyraFormProperties.setGridwidth(lyraForm.gridWidth());
            lyraFormProperties.setGridheight(lyraForm.gridHeight());
            lyraFormProperties.setHeader(lyraForm.gridHeader());
            lyraFormProperties.setFooter(lyraForm.gridFooter());
            lyraFormProperties.setVisibleColumnsHeader(lyraForm.visibleColumnsHeader());
            lyraFormProperties.setAllowTextSelection(lyraForm.allowTextSelection());
        }


        createUnboundField(fieldsMeta, PROPERTIES);


        this.context = context;
        rec = getCursor(context);
        rec.navigate("-");
        meta = rec.meta();
    }

    /**
     * A constructor for unit tests purposes only!
     */
    BasicLyraForm(DataGrainElement m) {
        meta = m;
    }

    /**
     * Adds all bound fields to meta information using their CelestaDoc.
     */
    public void createAllBoundFields() {
        int i = 0;
        for (Entry<String, ? extends ColumnMeta> e : meta.getColumns().entrySet()) {
            createBoundField(e.getKey(), i++, e.getValue());
        }
    }

    /**
     * Adds all unbound fields to meta information using their decorators'
     * parameters.
     */
    public void createAllUnboundFields() {
        createAllUnboundFields(fieldsMeta);
    }

    private static boolean getPropertyVal(JSONObject metadata, String propName, boolean def) throws JSONException {
        return metadata.has(propName) ? metadata.getBoolean(propName) : def;
    }

    private LyraFormField createBoundField(String name, int index, ColumnMeta m) {
        LyraFieldType lft = LyraFieldType.lookupFieldType(m);
        FieldAccessor a = FieldAccessorFactory.create(index, name, lft);
        LyraFormField f = new LyraFormField(name, a);
        fieldsMeta.addElement(f);
        f.setType(LyraFieldType.lookupFieldType(m));
        String json = CelestaDocUtils.getCelestaDocJSON(m.getCelestaDoc());
        try {
            JSONObject metadata = new JSONObject(json);
            f.setCaption(metadata.has(CAPTION) ? metadata.getString(CAPTION) : f.getName());
            f.setEditable(getPropertyVal(metadata, EDITABLE, true));
            f.setVisible(getPropertyVal(metadata, VISIBLE, true));
            if (metadata.has(SCALE)) {
                f.setScale(metadata.getInt(SCALE));
            } else {
                if (m instanceof StringColumn && !((StringColumn) m).isMax()) {
                    StringColumn sc = (StringColumn) m;
                    f.setScale(sc.getLength());
                } else if (m instanceof FloatingColumn) {
                    // Default for floating!
                    f.setScale(2);
                } else {
                    f.setScale(LyraFormField.DEFAULT_SCALE);
                }
            }

            if (m instanceof Column) {
                boolean dbRequired = !((Column) m).isNullable();
                f.setRequired(metadata.has(REQUIRED) ? metadata.getBoolean(REQUIRED) | dbRequired : dbRequired);
            } else {
                f.setRequired(getPropertyVal(metadata, REQUIRED, false));
            }

            // adding_field's_property
            f.setCssClassName(metadata.has(CSS_CLASS_NAME) ? metadata.getString(CSS_CLASS_NAME) : null);
            f.setCssStyle(metadata.has(CSS_STYLE) ? metadata.getString(CSS_STYLE) : null);
            f.setDateFormat(metadata.has(DATE_FORMAT) ? metadata.getInt(DATE_FORMAT) : DEFAULT_DATE_FORMAT);
            f.setDecimalSeparator(metadata.has(DECIMAL_SEPARATOR) ? metadata.getString(DECIMAL_SEPARATOR) : DEFAULT_DECIMAL_SEPARATOR);
            f.setGroupingSeparator(metadata.has(GROUPING_SEPARATOR) ? metadata.getString(GROUPING_SEPARATOR) : DEFAULT_GROUPING_SEPARATOR);

        } catch (JSONException e1) {
            throw new CelestaException("JSON Error: %s", e1.getMessage());
        }
        return f;
    }

    /**
     * Adds a specific field.
     *
     * @param name Name of a table column.
     */
    public LyraFormField createField(String name) {
        ColumnMeta m = meta.getColumns().get(name);
        if (m == null) {
            // UNBOUND FIELD
            LyraFormField result = createUnboundField(fieldsMeta, name);
            if (result == null) {
                throw new CelestaException(String.format("Column '%s' not found in '%s.%s'", name,
                        meta.getGrain().getName(), meta.getName()));
            }
            return result;
        } else {
            // BOUND FIELD
            // finding out field's index
            int index = 0;
            for (String n : meta.getColumns().keySet()) {
                if (n.equals(name)) {
                    break;
                }
                index++;
            }
            return createBoundField(name, index, m);
        }
    }

    /**
     * Sets call context for current form.
     *
     * @param context new call context.
     */
    public synchronized void setCallContext(CallContext context) {
        this.context = context;
    }

    /**
     * Gets current alive cursor.
     */
    // NB: never make this public, since we don't always have a correct
    // CallContext here!
    protected synchronized T rec() {
        if (rec == null) {
            if (context != null) {
                rec = getCursor(context);
                rec.navigate("-");
            }
        } else {
            if (rec.isClosed()) {
                T rec2 = getCursor(context);
                rec2.copyFieldsFrom(rec);
                rec = rec2;
                rec.navigate("=>+");
            }
        }
        return rec;
    }

    protected Cursor getCursor() {
        rec = rec();
        if (rec instanceof Cursor) {
            return (Cursor) rec;
        } else {
            throw new CelestaException("Cursor %s is not modifiable.", rec.meta().getName());
        }
    }

    /**
     * Returns form fields metadata.
     */
    public Map<String, LyraFormField> getFieldsMeta() {
        return fieldsMeta.getElements();
    }

    /**
     * Retrieves cursor's record metainformation.
     */
    public GrainElement meta() {
        return meta;
    }

    /**
     * Returns column names that are in sorting.
     */
    public String[] orderByColumnNames() {
        return rec == null ? null : rec.orderByColumnNames();
    }

    /**
     * Returns mask of DESC orders.
     */
    public boolean[] descOrders() {
        return rec == null ? null : rec.descOrders();
    }

    /*
     * These methods are named in Python style, not Java style. This is why
     * methods meant to be protected are called starting from underscore.
     */


    /**
     * Should return an active filtered and sorted cursor.
     */
    public abstract T getCursor(CallContext context);

    /**
     * Should return the form's fully qualified Python class name.
     */
    //public abstract String getId();
    public String getId() {
        return getClass().getName();
    }

    public LyraFormProperties getFormProperties() {
        return lyraFormProperties;
    }


    /**
     * Should append unbound field's meta information.
     *
     * @param meta Editable meta (NB: getFieldsMeta() returns read-only meta).
     * @param name Name of the field to be appended to form.
     */
    protected LyraFormField createUnboundField(LyraNamedElementHolder<LyraFormField> meta, String name) {

        LyraFormField f = null;

        try {
            String getterName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            Method getter = getClass().getMethod(getterName, CallContext.class);
            UnboundFieldAccessor ufa = new UnboundFieldAccessor(getter, null, this);
            f = new LyraFormField(name, ufa);
            meta.addElement(f);

            FormField formField = getter.getAnnotation(FormField.class);
            if (formField != null) {
                f.setCaption(formField.caption());
                f.setVisible(formField.visible());
                f.setEditable(formField.editable());
                f.setRequired(formField.required());
                f.setScale(formField.scale());
                f.setType(formField.type());
                f.setLookup(formField.lookup());

                // adding_field's_property
                f.setCssClassName(formField.cssClassName());
                f.setCssStyle(formField.cssStyle());
                f.setDateFormat(formField.dateFormat());
                f.setDecimalSeparator(formField.decimalSeparator());
                f.setGroupingSeparator(formField.groupingSeparator());

            }

            ufa.setLyraFieldType(f.getType());

        } catch (NoSuchMethodException e) {
            if (!PROPERTIES.equals(name)) {
                throw new CelestaException(
                        "Error %s while getting unbound field: %s. See logs for details.",
                        e.getClass().getName(), e.getMessage());
            }
        }

        return f;
    }


    /**
     * Should create all unbound fields
     *
     * @param fieldsMeta Editable meta (NB: getFieldsMeta() returns read-only meta).
     */
    protected void createAllUnboundFields(LyraNamedElementHolder<LyraFormField> fieldsMeta) {

        Method[] getters = getClass().getMethods();

        for (Method getter : getters) {
            if (getter.isAnnotationPresent(FormField.class)) {
                String fieldName = getter.getName().substring(3);
                fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);

                createUnboundField(fieldsMeta, fieldName);
            }
        }

    }


    public void beforeSending(BasicCursor c) {
    }


    CallContext getContext() {
        return context;
    }
}
