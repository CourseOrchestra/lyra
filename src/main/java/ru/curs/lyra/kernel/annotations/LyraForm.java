package ru.curs.lyra.kernel.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LyraForm {
    String gridWidth() default "";

    String gridHeight() default "";

    String gridHeader() default "";

    String gridFooter() default "";

    boolean visibleColumnsHeader() default true;

    boolean allowTextSelection() default true;

}
