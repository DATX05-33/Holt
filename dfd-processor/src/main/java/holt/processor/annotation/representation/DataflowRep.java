package holt.processor.annotation.representation;

import holt.processor.activator.ActivatorAggregate;

public record DataflowRep(ActivatorAggregate from, ActivatorAggregate to) {
}
