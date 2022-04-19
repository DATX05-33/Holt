package holt.applier;

import holt.activator.ProcessActivatorAggregate;
import holt.activator.QualifiedName;
import holt.activator.TraverseName;

import java.util.List;

public record FlowThroughRep(ProcessActivatorAggregate processActivator,
                             TraverseName traverseName,
                             String functionName,
                             QualifiedName outputType,
                             List<QueryRep> queries,
                             List<QueryDefinitionRep> overrideQueries) {

}
