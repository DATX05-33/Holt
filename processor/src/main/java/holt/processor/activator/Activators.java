package holt.processor.activator;

import java.util.List;

public record Activators(List<DatabaseActivator> databaseActivators,
                         List<ExternalEntityActivator> externalEntities,
                         List<ProcessActivator> processActivators) { }
