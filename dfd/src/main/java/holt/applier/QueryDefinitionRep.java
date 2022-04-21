package holt.applier;

import holt.activator.DatabaseActivatorAggregate;
import holt.activator.ProcessActivatorAggregate;

public record QueryDefinitionRep(
        DatabaseActivatorAggregate db,
        ProcessActivatorAggregate process,
        OutputRep outputRep
) {

}
