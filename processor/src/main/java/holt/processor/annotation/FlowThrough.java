package holt.processor.annotation;

import java.lang.annotation.*;


@Repeatable(FlowThroughs.class)
public @interface FlowThrough {
    Class<?> outputType() default Object.class;
    String flow();
    String functionName();
}
