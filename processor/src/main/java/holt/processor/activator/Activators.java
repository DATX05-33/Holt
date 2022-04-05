package holt.processor.activator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// This is information per DFD.
public record Activators(List<DatabaseActivatorAggregate> databaseActivators,
                         List<ExternalEntityActivatorAggregate> externalEntities,
                         List<ProcessActivatorAggregate> processActivators,
                         Map<TraverseName, List<ActivatorAggregate>> traverses) {
    public Stream<? extends ActivatorAggregate> stream() {
        return Stream.of(databaseActivators, externalEntities, processActivators).flatMap(Collection::stream);
    }

}
