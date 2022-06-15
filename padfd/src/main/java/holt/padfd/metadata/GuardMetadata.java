package holt.padfd.metadata;

import holt.Metadata;
import holt.activator.ActivatorId;

public record GuardMetadata(ActivatorId dataSourceActivator) implements Metadata {
}
