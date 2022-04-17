package holt;

import java.util.Map;

/**
 *
 */
public sealed interface OutputActivator permits ExternalEntityActivatorAggregate, DatabaseActivatorAggregate {
    void addOutput(TraverseName traverseName);
    void addInputToTraverseOutput(TraverseName traverseName, Connector connector);
    Map<TraverseName, TraverseOutput> outputs();
}
