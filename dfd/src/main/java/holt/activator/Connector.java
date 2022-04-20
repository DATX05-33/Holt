package holt.activator;

import java.util.Objects;

public final class Connector {

    private boolean isCollection;
    private QualifiedName type;

    public Connector() {
        isCollection = false;
    }

    public boolean isCollection() {
        return isCollection;
    }

    public void setCollection(boolean collection) {
        isCollection = collection;
    }

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
