package ru.curs.lyra.kernel;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.dbutils.Cursor;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import static ru.curs.lyra.kernel.LyraFormField.*;

/**
 * A serializable cursor data represention.
 */
public final class LyraFormData {
    private final LyraNamedElementHolder<LyraFieldValue> fields = new LyraNamedElementHolder<LyraFieldValue>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected String getErrorMsg(String name) {
            return "Field " + name + " is defined more than once in form data";
        }
    };
    private int recversion;
    private Object[] keyValues;

    private String formId;

    private SimpleDateFormat sdf;

    /**
     * Creates a serializable cursor data representation.
     *
     * @param c      A cursor.
     * @param map
     * @param formId Fully qualified form class name.
     */
    public LyraFormData(BasicCursor c, Map<String, LyraFormField> map, String formId) {
        if (c instanceof Cursor) {
            recversion = ((Cursor) c).getRecversion();
            keyValues = ((Cursor) c).getCurrentKeyValues();
        } else {
            // TODO: here we have an assumption that the first field is the key
            // field
            keyValues = new Object[1];
            keyValues[0] = c._currentValues()[0];
        }

        this.formId = formId;
        Object[] vals = c._currentValues();

        for (LyraFormField lff : map.values()) {
            Object val = lff.getAccessor().getValue(vals);
            LyraFieldValue lfv = new LyraFieldValue(lff, val);
            fields.addElement(lfv);
        }
    }

    public LyraFormData(InputStream is) {
        FormDataParser parser;
        parser = new FormDataParser();
        try {
            TransformerFactory.newInstance().newTransformer().transform(new StreamSource(is), new SAXResult(parser));
        } catch (Exception e) {
            throw new CelestaException("XML deserialization error: %s", e.getMessage());
        }
    }

    /**
     * Возвращает перечень полей.
     */
    public Collection<LyraFieldValue> getFields() {
        return fields;
    }

    /**
     * Передаёт значения в курсор.
     *
     * @param c   Курсор.
     * @param map Набор полей.
     */
    public void populateFields(Cursor c, Map<String, LyraFormField> map) {
        c.setRecversion(recversion);
        for (LyraFieldValue lfv : fields) {
            LyraFormField lff = map.get(lfv.getName());
            if (lff != null) {
                lff.getAccessor().setValue(c, lfv.getValue());
            }
        }
    }

    /**
     * Сериализует данные формы в поток в формате XML.
     *
     * @param outputStream Поток.
     */
    public void serialize(OutputStream outputStream) {
        try {
            XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance()
                    .createXMLStreamWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            xmlWriter.writeStartDocument();
            xmlWriter.writeStartElement("schema");
            xmlWriter.writeAttribute("recversion", Integer.toString(recversion));
            if (formId != null) {
                xmlWriter.writeAttribute("formId", formId);
            }

            Iterator<LyraFieldValue> i = fields.iterator();
            while (i.hasNext()) {
                i.next().serialize(xmlWriter);
            }
            xmlWriter.writeEndDocument();
            xmlWriter.flush();
        } catch (Exception e) {
            throw new CelestaException("XML Serialization error: %s", e.getMessage());
        }
    }

    /**
     * Recversion of serialized data.
     */
    public int getRecversion() {
        return recversion;
    }

    /**
     * Key values of serialized data.
     */
    public Object[] getKeyValues() {
        return keyValues;
    }

    /**
     * SAX-парсер сериализованного курсора.
     */
    private final class FormDataParser extends DefaultHandler {

        private final StringBuilder sb = new StringBuilder();
        private String key;
        private int status = 0;
        private boolean isNull = false;
        private LyraFieldType type = null;
        private int scale;
        private boolean required = false;

        private String cssClassName;
        private String cssStyle;

        private String dateFormat = DEFAULT_DATE_FORMAT;
        private String decimalSeparator = DEFAULT_DECIMAL_SEPARATOR;
        private String groupingSeparator = DEFAULT_GROUPING_SEPARATOR;


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            switch (status) {
                case 0:
                    recversion = Integer.parseInt(attributes.getValue("recversion"));
                    formId = attributes.getValue("formId");
                    status = 1;
                    break;
                case 1:
                    key = localName;
                    type = LyraFieldType.valueOf(attributes.getValue("type"));

                    String buf = attributes.getValue("null");
                    isNull = buf == null ? false : Boolean.parseBoolean(buf);

                    buf = attributes.getValue(SCALE);
                    scale = buf == null ? LyraFormField.DEFAULT_SCALE : Integer.parseInt(buf);

                    buf = attributes.getValue(REQUIRED);
                    required = buf == null ? false : Boolean.parseBoolean(buf);


                    // adding_field's_property
                    buf = attributes.getValue(CSS_CLASS_NAME);
                    cssClassName = buf;

                    buf = attributes.getValue(CSS_STYLE);
                    cssStyle = buf;

                    buf = attributes.getValue(DATE_FORMAT);
                    dateFormat = buf;

                    buf = attributes.getValue(DECIMAL_SEPARATOR);
                    decimalSeparator = buf;

                    buf = attributes.getValue(GROUPING_SEPARATOR);
                    groupingSeparator = buf;


                    status = 2;
                    sb.setLength(0);
                default:
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (status == 2) {
                sb.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (status == 2) {
                status = 1;
                try {
                    LyraFieldValue v;
                    LyraFormField lff = new LyraFormField(key);
                    lff.setScale(scale);
                    lff.setRequired(required);
                    lff.setType(type);


                    // adding_field's_property
                    lff.setCssClassName(cssClassName);
                    lff.setCssStyle(cssStyle);
                    lff.setDateFormat(dateFormat);
                    lff.setDecimalSeparator(decimalSeparator);
                    lff.setGroupingSeparator(groupingSeparator);


                    if (isNull && sb.length() == 0) {
                        v = new LyraFieldValue(lff, null);
                    } else {
                        String buf = sb.toString();
                        switch (type) {
                            case DATETIME:
                                if (sdf == null) {
                                    sdf = new SimpleDateFormat(LyraFieldValue.XML_DATE_FORMAT);
                                }
                                Date d;
                                try {
                                    d = sdf.parse(buf);
                                } catch (java.text.ParseException e) {
                                    d = null;
                                }
                                v = new LyraFieldValue(lff, d);
                                break;
                            case BIT:
                                v = new LyraFieldValue(lff, Boolean.valueOf(buf));
                                break;
                            case INT:
                                v = new LyraFieldValue(lff, Integer.valueOf(buf));
                                break;
                            case REAL:
                                v = new LyraFieldValue(lff, Double.valueOf(buf));
                                break;
                            default:
                                v = new LyraFieldValue(lff, buf);
                        }
                    }
                    fields.addElement(v);
                } catch (CelestaException e) {
                    throw new SAXException(e.getMessage());
                }
            }
        }

    }
}
