package holt.applier;

import holt.activator.Domain;

import java.util.List;

public final class TraverseApplier {

    private TraverseApplier() { }

    public static void applyTraverseRep(List<TraverseRep> traverseReps) {
        for (TraverseRep traverseRep : traverseReps) {
            traverseRep.externalEntityActivator().setOutputType(traverseRep.name(), traverseRep.flowStartType());
        }
    }

}
