package holt.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Query {
    Class<?> db();
    Class<?> type() default Object.class;
}
