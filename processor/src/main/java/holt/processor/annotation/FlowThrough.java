package holt.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Repeatable(FlowThroughs.class)
public @interface FlowThrough {
    Output output();
    String traverse();
    String functionName();
    Query[] queries() default {};

    QueryDefinition[] overrideQueries() default {};

    /**
     * If the activator is responsible for more than one requirement,
     * then you need to specify the forActivator to know which one this @FlowThrough is meant for.
     */
    String forActivator() default "";
}
