package holt.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Repeatable(DFDs.class)
public @interface DFD {
    String name();
    String xml();
    boolean privacyAware() default false;
}
