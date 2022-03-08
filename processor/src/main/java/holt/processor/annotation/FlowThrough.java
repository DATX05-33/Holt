package holt.processor.annotation;

import holt.processor.Flow;

import java.lang.annotation.*;


@Repeatable(FlowThroughs.class)
public @interface FlowThrough {
    Class<?> outputType() default Object.class;
    String flow();
    String functionName();
}
