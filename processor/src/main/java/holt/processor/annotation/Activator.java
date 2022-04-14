package holt.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Activator {
    /**
     * Name of the activator that this class represents in the DFD.
     * This only needs to be set if and only if the class name and name of the activator in the DFD differs.
     */
    String graphName() default "";
}
