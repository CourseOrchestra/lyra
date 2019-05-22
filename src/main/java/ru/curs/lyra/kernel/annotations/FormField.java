package ru.curs.lyra.kernel.annotations;

import ru.curs.lyra.kernel.LyraFormField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormField {
    String caption() default "";

    boolean visible() default true;

    boolean editable() default true;

    boolean sortable() default true;

    boolean required() default false;

    int scale() default LyraFormField.DEFAULT_SCALE;

    String lookup() default "";

    // adding_field's_property
    String cssClassName() default "";

    String cssStyle() default "";

    String dateFormat() default LyraFormField.DEFAULT_DATE_FORMAT;

    String decimalSeparator() default LyraFormField.DEFAULT_DECIMAL_SEPARATOR;

    String groupingSeparator() default LyraFormField.DEFAULT_GROUPING_SEPARATOR;

}
