package holt;

import java.util.Objects;

public final class Connector {

    private QualifiedName type;

    public void setType(QualifiedName type) {
        this.type = type;
    }

    public QualifiedName type() {
        return Objects.requireNonNullElseGet(this.type, () -> QualifiedName.OBJECT);
    }

    @Override
    public String toString() {
        return "Connector{" +
                "hashcode=" + super.hashCode() +
                ", type=" + type +
                '}';
    }
}
