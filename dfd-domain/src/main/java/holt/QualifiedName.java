package holt;

import java.util.Objects;

public record QualifiedName(String value) {
    public QualifiedName {
        Objects.requireNonNull(value);
    }
    public static final QualifiedName OBJECT = new QualifiedName("java.lang.Object");
    public String simpleName() {
        String[] split = value.split("\\.");
        return split[Math.max(split.length - 1, 0)];
    }
}
