package holt.processor.activator;

/**
 *
 * @param source Where the query definition resides.
 */
public record QueryDefinition(ProcessActivatorAggregate source, Flow flow) {
}
