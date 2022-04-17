package holt.representation;

import holt.ProcessActivatorAggregate;
import holt.QualifiedName;
import holt.TraverseName;

import java.util.List;

public record FlowThroughRep(ProcessActivatorAggregate processActivator,
                             TraverseName traverseName,
                             String functionName,
                             QualifiedName outputType,
                             List<QueryRep> queries,
                             List<QueryDefinitionRep> overrideQueries) {

}
