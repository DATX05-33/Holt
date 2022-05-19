package holt.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Activator {
    /**
     * Name of the activator that this class represents in the DFD.
     * This only needs to be set if and only if the class name and name of the activator in the DFD differs.
     */
    String[] graphName() default {};

    /**
     * If true, then there will not be a get*Instance method generated in the relevant external entities.
     * Reflection will instead be used. Note that then, there cannot be any arguments.
     */
    boolean instantiateWithReflection() default false;
}
