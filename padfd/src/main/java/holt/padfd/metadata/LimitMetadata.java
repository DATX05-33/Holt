package holt.padfd.metadata;

import holt.Metadata;
import holt.activator.ActivatorId;

/**
 *
 * @param dataSourceActivator The activator that have the correct QualifiedName.
 */
public record LimitMetadata(ActivatorId dataSourceActivator) implements Metadata {

}
