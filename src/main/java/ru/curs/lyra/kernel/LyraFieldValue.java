package ru.curs.lyra.kernel;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static ru.curs.lyra.kernel.LyraFormField.*;

/**
 * Значение поля, передаваемого в форму и обратно.
 */
public final class LyraFieldValue extends LyraNamedElement {
    static final String XML_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private final Object val;
    private final LyraFormField lff;

    LyraFieldValue(LyraFormField lff, Object val) {
        super(lff.getName());
        this.lff = lff;
        this.val = val;
    }

    /**
     * Сериализация.
     *
     * @param xmlWriter Объект, в который записывается XML-поток.
     * @throws XMLStreamException Ошибка записи в поток.
     */
    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartElement(getName());
        xmlWriter.writeAttribute("type", lff.getType().toString());


        // adding_field's_property
        if (lff.getCssClassName() != null) {
            xmlWriter.writeAttribute(CSS_CLASS_NAME, lff.getCssClassName());
        }
        if (lff.getCssStyle() != null) {
            xmlWriter.writeAttribute(CSS_STYLE, lff.getCssStyle());
        }

        xmlWriter.writeAttribute(DATE_FORMAT, lff.getDateFormat());

        if (lff.getDecimalSeparator() != null) {
            xmlWriter.writeAttribute(DECIMAL_SEPARATOR, lff.getDecimalSeparator());
        }

        if (lff.getGroupingSeparator() != null) {
            xmlWriter.writeAttribute(GROUPING_SEPARATOR, lff.getGroupingSeparator());
        }


        if (val == null) {
            xmlWriter.writeAttribute("null", Boolean.toString(true));
        }
        if (lff.getScale() != DEFAULT_SCALE) {
            xmlWriter.writeAttribute(SCALE, Integer.toString(lff.getScale()));
        }
        if (lff.isRequired()) {
            xmlWriter.writeAttribute(REQUIRED, Boolean.toString(true));
        }

        if (val instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat(XML_DATE_FORMAT);
            xmlWriter.writeCharacters(sdf.format(val));
        } else {
            xmlWriter.writeCharacters(val == null ? "" : val.toString());
        }
        xmlWriter.writeEndElement();
    }

    /**
     * Значение поля.
     */
    public Object getValue() {
        return val;
    }

    /**
     * Метаданные поля.
     */
    public LyraFormField meta() {
        return lff;
    }

}
