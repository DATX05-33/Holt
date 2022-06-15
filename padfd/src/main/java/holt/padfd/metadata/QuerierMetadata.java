package holt.padfd.metadata;

import holt.Metadata;
import holt.activator.ActivatorId;

/**
 *
 * @param database Database that is queried from
 * @param process Process that the query definition should be moved to
 */
public record QuerierMetadata(ActivatorId database, ActivatorId process) implements Metadata {

}
