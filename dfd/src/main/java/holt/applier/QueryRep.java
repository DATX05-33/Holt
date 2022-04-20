package holt.applier;

import holt.activator.QualifiedName;

public record QueryRep(QualifiedName db, QualifiedName type, boolean isCollection) {

}
