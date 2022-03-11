package holt.processor.activator;

import java.util.List;

public record Activators(List<Database> databases,
                         List<ExternalEntity> externalEntities,
                         List<Process> processes) { }
