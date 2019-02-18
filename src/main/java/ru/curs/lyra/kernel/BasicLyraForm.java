package ru.curs.lyra.kernel;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.ReflectionUtils;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.dbutils.Cursor;
import ru.curs.celesta.score.*;
import ru.curs.lyra.kernel.annotations.FormField;
import ru.curs.lyra.kernel.annotations.LyraForm;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ru.curs.lyra.kernel.LyraFormField.*;

/**
 * Base Java class for Lyra forms. Two classes inherited from this one are
 * BasicCardForm and BasicGridForm.
 */
public abstract class BasicLyraForm<T extends BasicCursor> {

    public static final String PROPERTIES = "recordProperties";
    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^(is|get)([A-Z])([^$]*$)");

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
            lyraFormProperties.setGridwidth(lyraForm.gridWidth())
                    .setGridheight(lyraForm.gridHeight())
                    .setHeader(lyraForm.gridHeader())
                    .setFooter(lyraForm.gridFooter())
                    .setVisibleColumnsHeader(lyraForm.visibleColumnsHeader())
                    .setAllowTextSelection(lyraForm.allowTextSelection());
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
                boolean dbRequired = !m.isNullable();
                f.setRequired(metadata.has(REQUIRED) ? metadata.getBoolean(REQUIRED) | dbRequired : dbRequired);
            } else {
                f.setRequired(getPropertyVal(metadata, REQUIRED, false));
            }

            // adding_field's_property
            f.setCssClassName(metadata.has(CSS_CLASS_NAME) ? metadata.getString(CSS_CLASS_NAME) : null);
            f.setCssStyle(metadata.has(CSS_STYLE) ? metadata.getString(CSS_STYLE) : null);
            f.setDateFormat(metadata.has(DATE_FORMAT)
                    ? metadata.getInt(DATE_FORMAT) : DEFAULT_DATE_FORMAT);
            f.setDecimalSeparator(metadata.has(DECIMAL_SEPARATOR)
                    ? metadata.getString(DECIMAL_SEPARATOR) : DEFAULT_DECIMAL_SEPARATOR);
            f.setGroupingSeparator(metadata.has(GROUPING_SEPARATOR)
                    ? metadata.getString(GROUPING_SEPARATOR) : DEFAULT_GROUPING_SEPARATOR);

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


    private static boolean isGetter(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0 || (
                parameterTypes.length == 1
                        && parameterTypes[0].equals(CallContext.class))) {
            if (method.getName().matches("^get[A-Z].*")
                    && !method.getReturnType().equals(void.class)) {
                return true;
            }
            if (method.getName().matches("^is[A-Z].*")
                    && method.getReturnType().equals(boolean.class)) {
                return true;
            }
        }
        return false;
    }

    private String extractFieldName(Method m) {
        Matcher matcher = FIELD_NAME_PATTERN.matcher(m.getName());
        matcher.matches();
        return matcher.group(2).toLowerCase() + matcher.group(3);
    }

    /**
     * Should append unbound field's meta information.
     *
     * @param meta Editable meta (NB: getFieldsMeta() returns read-only meta).
     * @param name Name of the field to be appended to form.
     */
    protected LyraFormField createUnboundField(LyraNamedElementHolder<LyraFormField> meta, String name) {
        final List<LyraFormField> newFields = getUnboundFields(method ->
                name.equals(extractFieldName(method))
        );
        if (newFields.size() == 0) {
            if (PROPERTIES.equals(name)) {
                return null;
            } else {
                throw new CelestaException("Field '%s' has no annotated getters", name);
            }
        } else if (newFields.size() > 1) {
            String getterNames = newFields.stream().map(LyraNamedElement::getName).collect(Collectors.joining(","));
            throw new CelestaException("Field '%s' has too many annotated getters: %s", name, getterNames);
        } else {
            LyraFormField newField = newFields.get(0);
            meta.addElement(newField);
            return newField;
        }
    }

    private List<LyraFormField> getUnboundFields(Predicate<Method> filter) {
        final List<LyraFormField> newFields = new ArrayList<>();
        ReflectionUtils.doWithLocalMethods(getClass(),
                getter -> {
                    if (isGetter(getter)
                            && getter.isAnnotationPresent(FormField.class)
                            && filter.test(getter)
                    ) {
                        UnboundFieldAccessor ufa = new UnboundFieldAccessor(getter, null, this);
                        LyraFormField f = new LyraFormField(extractFieldName(getter), ufa);
                        newFields.add(f);
                        FormField formField = getter.getAnnotation(FormField.class);
                        LyraFieldType lyraFieldType = LyraFieldType.lookupFieldType(getter.getReturnType());
                        if (formField != null) {
                            f.setCaption(formField.caption())
                                    .setVisible(formField.visible())
                                    .setEditable(formField.editable())
                                    .setRequired(formField.required())
                                    .setScale(formField.scale())
                                    .setType(lyraFieldType)
                                    .setLookup(formField.lookup())
                                    // adding_field's_property
                                    .setCssClassName(formField.cssClassName())
                                    .setCssStyle(formField.cssStyle())
                                    .setDateFormat(formField.dateFormat())
                                    .setDecimalSeparator(formField.decimalSeparator())
                                    .setGroupingSeparator(formField.groupingSeparator());
                        }
                    }
                });
        return newFields;
    }


    /**
     * Should create all unbound fields
     *
     * @param fieldsMeta Editable meta (NB: getFieldsMeta() returns read-only meta).
     */
    protected void createAllUnboundFields(LyraNamedElementHolder<LyraFormField> fieldsMeta) {
        List<LyraFormField> unboundFields = getUnboundFields(m -> true);
        for (LyraFormField field : unboundFields) {
            fieldsMeta.addElement(field);
        }
    }


    public void beforeSending(BasicCursor c) {
    }


    CallContext getContext() {
        return context;
    }
}
