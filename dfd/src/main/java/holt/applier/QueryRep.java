package holt.applier;

import holt.activator.QualifiedName;

public record QueryRep(QualifiedName db, OutputRep outputRep) {
    @Override
    public String toString() {
        return "QueryRep{" +
                "db=" + db.value() +
                ", outputRep=" + outputRep +
                '}';
    }
}
