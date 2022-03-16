package holt.processor.annotation;

public @interface Query {
    Class<?> db();
    Class<?> type() default Object.class;
}
