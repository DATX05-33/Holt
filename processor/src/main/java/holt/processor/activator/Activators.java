package holt.processor.activator;

import java.util.List;
import java.util.Map;

public record Activators(List<DatabaseActivator> databaseActivators,
                         List<ExternalEntityActivator> externalEntities,
                         List<ProcessActivator> processActivators,
                         Map<FlowName, List<Activator>> flows) { }
