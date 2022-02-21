package Holt.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface ActivatorProcess {
    Class input();
    Class output();
    String methodName();
}
