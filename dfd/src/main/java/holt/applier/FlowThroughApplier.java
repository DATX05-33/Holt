package holt.applier;

import holt.activator.ActivatorId;
import holt.activator.DatabaseActivatorAggregate;
import holt.activator.FlowOutput;
import holt.activator.FlowThroughAggregate;
import holt.activator.FunctionName;
import holt.activator.QueryInput;
import holt.activator.TraverseName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FlowThroughApplier {

    private FlowThroughApplier() { }

    public static void applyFlowThrough(List<FlowThroughRep> flowThroughReps) {
        for (FlowThroughRep flowThroughRep : flowThroughReps) {
            TraverseName traverseName = flowThroughRep.traverseName();
            OutputRep outputRep = flowThroughRep.outputRep();
            FlowThroughAggregate flowThrough = flowThroughRep.processActivator().flow(traverseName);
            flowThrough.setOutputType(outputRep.type(), outputRep.collection());
            flowThrough.setFunctionName(new FunctionName(flowThroughRep.functionName()));

            flowThroughRep.queries().forEach(query -> {
                OutputRep queryOutputRep = query.outputRep();
                boolean found = false;
                for (QueryInput queryInput : flowThrough.queries()) {
                    DatabaseActivatorAggregate databaseActivator = queryInput.queryInputDefinition().database();
                    if (databaseActivator.name().value().equals(query.db().simpleName())) {
                        found = true;
                        queryInput.queryInputDefinition().setOutput(
                                new FlowOutput(
                                        queryOutputRep.type(),
                                        queryOutputRep.collection()
                                )
                        );
                    }
                }

                if (!found) {
                    // Then, this flow through is setting something that might be moved to this process after the PADFDEnhancer.
                    // The following is sort of a cache
                    flowThrough.addLaterQuerySetup(query);
                }

            });

            for (QueryDefinitionRep queryDefinitionRep : flowThroughRep.overrideQueries()) {
                OutputRep queryDefinitionOutputRep = queryDefinitionRep.outputRep();
                queryDefinitionRep.process().flow(traverseName).moveQueryInputDefinitionTo(
                        queryDefinitionRep.db(),
                        flowThrough,
                        new FlowOutput(
                                queryDefinitionOutputRep.type(),
                                queryDefinitionOutputRep.collection()
                        )
                );
            }
        }
    }

}
