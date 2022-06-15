package holt.applier;

import holt.activator.FlowOutput;

import java.util.List;

public final class TraverseApplier {

    private TraverseApplier() { }

    public static void applyTraverseRep(List<TraverseRep> traverseReps) {
        for (TraverseRep traverseRep : traverseReps) {
            traverseRep.externalEntityActivator().addStart(
                    traverseRep.name(),
                    traverseRep.startTypes()
                            .stream()
                            .map(outputRep -> new FlowOutput(
                                    outputRep.type(),
                                    outputRep.collection()
                            ))
                            .toList()
            );
        }
    }

}
