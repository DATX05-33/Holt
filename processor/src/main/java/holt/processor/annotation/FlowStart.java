package holt.processor.annotation;

import java.lang.annotation.Repeatable;

@Repeatable(FlowStarts.class)
public @interface FlowStart {
    Class<?> flowStartType();
    String flow();
}
