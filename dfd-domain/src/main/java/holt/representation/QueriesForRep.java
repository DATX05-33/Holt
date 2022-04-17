package holt.representation;

import holt.DatabaseActivatorAggregate;
import holt.QualifiedName;

public record QueriesForRep(DatabaseActivatorAggregate databaseActivatorAggregate,
                            QualifiedName queriesClassName) { }
