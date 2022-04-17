package holt.representation;

import holt.ExternalEntityActivatorAggregate;
import holt.QualifiedName;
import holt.TraverseName;

public record TraverseRep(
        TraverseName name,
        QualifiedName flowStartType,
        String[] dataflows,
        ExternalEntityActivatorAggregate externalEntityActivator) {



}
