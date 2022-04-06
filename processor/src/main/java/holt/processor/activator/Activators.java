package holt.processor.activator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// This is information per DFD.
public record Activators(List<ActivatorAggregate> activators,
                         Map<TraverseName, List<ActivatorAggregate>> traverses) {

    public Stream<DatabaseActivatorAggregate> databases() {
        return activators.stream()
                .filter(activatorAggregate -> activatorAggregate instanceof DatabaseActivatorAggregate)
                .map(activatorAggregate -> (DatabaseActivatorAggregate) activatorAggregate);
    }

    public Stream<ExternalEntityActivatorAggregate> externalEntities() {
        return activators.stream()
                .filter(activatorAggregate -> activatorAggregate instanceof ExternalEntityActivatorAggregate)
                .map(activatorAggregate -> (ExternalEntityActivatorAggregate) activatorAggregate);
    }

    public Stream<ProcessActivatorAggregate> processes() {
        return activators.stream()
                .filter(activatorAggregate -> activatorAggregate instanceof ProcessActivatorAggregate)
                .map(activatorAggregate -> (ProcessActivatorAggregate) activatorAggregate);
    }

    public Stream<ActivatorAggregate> stream() {
        return activators.stream();
    }

}
