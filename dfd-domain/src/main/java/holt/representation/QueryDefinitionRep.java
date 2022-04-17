package holt.representation;

import holt.DatabaseActivatorAggregate;
import holt.ProcessActivatorAggregate;
import holt.QualifiedName;

public record QueryDefinitionRep(DatabaseActivatorAggregate db, ProcessActivatorAggregate process, QualifiedName type) {

}
