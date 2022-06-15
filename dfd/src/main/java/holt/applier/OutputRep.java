package holt.applier;

import holt.activator.QualifiedName;

public record OutputRep(QualifiedName type, boolean collection) {
    @Override
    public String toString() {
        return "OutputRep{" +
                "type=" + type.value() +
                ", collection=" + collection +
                '}';
    }
}
