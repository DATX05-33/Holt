package holt.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Activator {
    /**
     * If true, then there will not be a get*Instance method generated in the relevant external entities.
     * Reflection will instead be used. Note that then, there cannot be any arguments.
     */
    boolean instantiateWithReflection() default false;
}
