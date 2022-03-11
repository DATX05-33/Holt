package holt.processor.activator;

import java.util.List;
import java.util.stream.Stream;

public record Activators(List<Database> databaseBonds,
                         List<ExternalEntity> externalEntityBonds,
                         List<Process> processBonds) {

    public Stream<Activator> nodeStream() {
        return Stream.of(databaseBonds, externalEntityBonds, processBonds)
                .flatMap(entities -> entities.stream().map(entity -> (Activator) entity));
    }

}
