package holt.applier;

import holt.activator.DatabaseActivatorAggregate;
import holt.activator.Domain;
import holt.activator.FlowThroughAggregate;
import holt.activator.FunctionName;
import holt.activator.QueryInput;
import holt.activator.TraverseName;

import java.util.List;

public final class FlowThroughApplier {

    private FlowThroughApplier() { }

    public static void applyFlowThrough(List<FlowThroughRep> flowThroughReps) {
        for (FlowThroughRep flowThroughRep : flowThroughReps) {
            TraverseName traverseName = flowThroughRep.traverseName();
            FlowThroughAggregate flowThrough = flowThroughRep.processActivator().flow(traverseName);
            flowThrough.setOutputType(flowThroughRep.outputType(), flowThroughRep.outputIsCollection());
            flowThrough.setFunctionName(new FunctionName(flowThroughRep.functionName()));

            flowThroughRep.queries().forEach(query -> {
                for (QueryInput queryInput : flowThrough.queries()) {
                    DatabaseActivatorAggregate databaseActivator = queryInput.queryInputDefinition().database();
                    if (databaseActivator.name().value().equals(query.db().simpleName())) {
                        queryInput.queryInputDefinition().setOutput(query.type());
                    }
                }
            });

            for (QueryDefinitionRep queryDefinitionRep : flowThroughRep.overrideQueries()) {
                queryDefinitionRep.process().flow(traverseName).moveQueryInputDefinitionTo(
                        queryDefinitionRep.db(),
                        flowThrough
                );
            }
        }
    }

}
