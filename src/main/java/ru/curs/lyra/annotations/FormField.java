package ru.curs.lyra.annotations;

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
    boolean required() default  false;
    int scale() default 0;
    int width() default 0;
}
