package holt.processor.activator;

import com.squareup.javapoet.ClassName;

import java.util.Objects;

public final class Connector {

    private ClassName type;

    public void setType(ClassName type) {
        this.type = type;
    }

    public ClassName type() {
        return Objects.requireNonNullElseGet(this.type, () -> ClassName.OBJECT);
    }

    @Override
    public String toString() {
        return "Connector{" +
                "hashcode=" + super.hashCode() +
                ", type=" + type +
                '}';
    }
}
