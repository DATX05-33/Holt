package holt.processor.annotation;

import java.lang.annotation.Repeatable;

@Repeatable(Traverses.class)
public @interface Traverse {
    Class<?> flowStartType() default Object.class;
    String name();
    String[] order();
}
