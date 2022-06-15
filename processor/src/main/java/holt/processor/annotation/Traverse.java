package holt.processor.annotation;

import java.lang.annotation.Repeatable;

@Repeatable(Traverses.class)
public @interface Traverse {
    Output[] startTypes() default {};
    String name();
    String[] order();
}
