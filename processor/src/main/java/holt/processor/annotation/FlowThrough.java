package holt.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Repeatable(FlowThroughs.class)
public @interface FlowThrough {
    Class<?> outputType() default Object.class;
    String traverse();
    String functionName();
    Query[] queries() default {};
    QueryDefinition[] overrideQueries() default {};
}
