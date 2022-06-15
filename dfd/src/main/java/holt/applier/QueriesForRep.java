package holt.applier;

import holt.activator.DatabaseActivatorAggregate;
import holt.activator.QualifiedName;

public record QueriesForRep(DatabaseActivatorAggregate databaseActivatorAggregate,
                            QualifiedName queriesClassName) { }
