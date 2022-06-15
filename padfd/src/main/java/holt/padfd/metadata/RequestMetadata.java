package holt.padfd.metadata;

import holt.Metadata;
import holt.activator.ActivatorId;

public record RequestMetadata(ActivatorId dataSourceActivator) implements Metadata {
}
