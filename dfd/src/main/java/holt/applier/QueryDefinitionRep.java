package holt.applier;

import holt.activator.DatabaseActivatorAggregate;
import holt.activator.ProcessActivatorAggregate;
import holt.activator.QualifiedName;

public record QueryDefinitionRep(
        DatabaseActivatorAggregate db,
        ProcessActivatorAggregate process,
        QualifiedName type,
        boolean isCollection
) {

}
