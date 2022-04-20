package holt.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface QueryDefinition {
    Class<?> db();
    Class<?> process();
    Class<?> type();
    boolean isCollection() default false;
}
