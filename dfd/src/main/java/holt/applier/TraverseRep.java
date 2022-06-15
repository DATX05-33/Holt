package holt.applier;

import holt.activator.ExternalEntityActivatorAggregate;
import holt.activator.TraverseName;

import java.util.List;

public record TraverseRep(
        TraverseName name,
        List<OutputRep> startTypes,
        String[] dataflows,
        ExternalEntityActivatorAggregate externalEntityActivator) {



}
