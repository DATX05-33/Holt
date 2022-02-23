package holt.processor.annotation;

public @interface Activator {
    String methodName();
    Class<?> outputType();
}
