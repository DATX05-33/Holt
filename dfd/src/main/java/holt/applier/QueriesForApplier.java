package holt.applier;

import holt.activator.Domain;

import java.util.List;

public final class QueriesForApplier {

    private QueriesForApplier() { }

    public static void applyQueriesFor(List<QueriesForRep> queriesForReps) {
        for (QueriesForRep queriesForRep : queriesForReps) {
            // There can only be one queries for per database
            if (queriesForRep.databaseActivatorAggregate().getQueriesClassName() != null) {
                throw new IllegalStateException("There can only be one @QueriesFor per database");
            }

            queriesForRep.databaseActivatorAggregate().setQueriesClassName(queriesForRep.queriesClassName());
        }
    }

}
