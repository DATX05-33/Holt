package holt.processor.annotation;

public @interface Query {
    String db();
    Class<?> type() default Object.class;
}
