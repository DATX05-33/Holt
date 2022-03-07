package holt.processor.annotation;

import holt.processor.Flow;

import java.lang.annotation.*;


@Repeatable(FlowThroughs.class)
public @interface FlowThrough {
    Class<?> outputType();
    String flow();
    String functionName();
}
