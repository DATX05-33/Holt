package holt.processor.annotation;

public @interface Output {
    Class<?> type() default Object.class;
    boolean collection() default false;
}
