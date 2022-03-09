package holt.processor.annotation;

public @interface Query {
    Class<?> value() default Object.class;
}
