package holt.processor.annotation.representation;

import com.squareup.javapoet.ClassName;
import holt.processor.activator.DatabaseActivatorAggregate;

public record QueriesForRep(DatabaseActivatorAggregate databaseActivatorAggregate,
                            ClassName queriesClassName) { }
