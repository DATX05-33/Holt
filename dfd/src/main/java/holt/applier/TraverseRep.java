package holt.applier;

import holt.activator.ExternalEntityActivatorAggregate;
import holt.activator.QualifiedName;
import holt.activator.TraverseName;

public record TraverseRep(
        TraverseName name,
        QualifiedName flowStartType,
        String[] dataflows,
        ExternalEntityActivatorAggregate externalEntityActivator) {



}
