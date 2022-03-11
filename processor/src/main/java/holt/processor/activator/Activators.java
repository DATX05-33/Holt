package holt.processor.activator;

import java.util.List;
import java.util.stream.Stream;

public record Activators(List<Database> databases,
                         List<ExternalEntity> externalEntities,
                         List<Process> processes) {

    public Stream<Activator> nodeStream() {
        return Stream.of(databases, externalEntities, processes)
                .flatMap(entities -> entities.stream().map(entity -> (Activator) entity));
    }

}
