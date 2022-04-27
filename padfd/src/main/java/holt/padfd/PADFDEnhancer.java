package holt.padfd;

import holt.activator.ActivatorAggregate;
import holt.activator.ActivatorId;
import holt.activator.Connector;
import holt.activator.DatabaseActivatorAggregate;
import holt.activator.Domain;
import holt.activator.ExternalEntityActivatorAggregate;
import holt.activator.FlowOutput;
import holt.activator.FlowThroughAggregate;
import holt.activator.ProcessActivatorAggregate;
import holt.activator.QualifiedName;
import holt.activator.TraverseName;
import holt.padfd.metadata.CombineMetadata;
import holt.padfd.metadata.GuardMetadata;
import holt.padfd.metadata.LimitMetadata;
import holt.padfd.metadata.QuerierMetadata;
import holt.padfd.metadata.RequestMetadata;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class PADFDEnhancer {

    private PADFDEnhancer() {}

    public static void enhance(Domain domain, ProcessingEnvironment processingEnvironment) {
        /*
         * Go through all activator aggregates that are connected to the first external entity aggregate.
         * Separate so that all request activators only receive the policy connectors, and
         * that the limit nodes only get the data connectors.
         *
         * A connector is considered a policy connector if that class implements Policy.
         */
        domain.traverses().forEach((traverseName, activatorAggregates) -> {

            ExternalEntityActivatorAggregate externalEntity = (ExternalEntityActivatorAggregate) activatorAggregates.get(0);
            List<Connector> startConnectors = externalEntity.starts().get(traverseName);
            Map<Connector, Boolean> isPolicyMap = new HashMap<>();

            startConnectors.forEach(connector -> isPolicyMap.put(connector, isPolicyConnector(connector, processingEnvironment)));

            if (startConnectors.size() != 2
                    && (isPolicyMap.get(startConnectors.get(0)) ^ isPolicyMap.get(startConnectors.get(1)))) {
                throw new IllegalStateException("External entity must have exactly two outputs. One policy and one data.");
            }

            // The following code can in theory handle more than 2 inputs, but to make sure other things work, we need to limit it to two.
            for (ActivatorAggregate activatorAggregate : activatorAggregates) {
                if (activatorAggregate instanceof ProcessActivatorAggregate processActivatorAggregate) {
                    FlowThroughAggregate flow = processActivatorAggregate.flow(traverseName);

                    // Before removing the extra input activator, set the proper output
                    if (processActivatorAggregate.metadata() instanceof RequestMetadata requestMetadata) {
                        if (processActivatorAggregate.flows().size() != 1) {
                            throw new IllegalStateException("Limit can only have one flow");
                        }

                        ActivatorAggregate sourceActivator = getActivatorAggregate(requestMetadata.dataSourceActivator(), domain.activators());
                        QualifiedName dataQualifiedName;
                        if (sourceActivator instanceof ProcessActivatorAggregate p) {
                            dataQualifiedName = QualifiedName.of(p.getOutput(traverseName), true);
                        } else if (sourceActivator instanceof ExternalEntityActivatorAggregate e) {
                            dataQualifiedName = QualifiedName.of(e.starts().get(traverseName).stream().filter(key -> !isPolicyMap.get(key)).findFirst().orElseThrow());
                        } else {
                            throw new IllegalStateException();
                        }

                        flow.setOutputType(
                                QualifiedName.of(
                                        Map.class.getName(),
                                        List.of(
                                                dataQualifiedName,
                                                processActivatorAggregate.getOutput(traverseName).flowOutput().type()
                                        )
                                ),
                                false
                        );
                    }

                    for (Connector flowInput : flow.inputs()) {
                        if (!isPolicyMap.containsKey(flowInput)) {
                            continue;
                        }

                        if (activatorAggregate.metadata() instanceof RequestMetadata) {
                            // Input is not a policy connector
                            if (!isPolicyMap.get(flowInput)) {
                                flow.removeInput(flowInput);
                            }
                        } else {
                            // Input is a policy connector
                            if (isPolicyMap.get(flowInput)) {
                                flow.removeInput(flowInput);
                            }
                        }
                    }
                }
            }
        });

        /*
         *
         */
        for (ActivatorAggregate activator : domain.activators()) {
            if (activator instanceof ProcessActivatorAggregate processActivatorAggregate) {
                if (processActivatorAggregate.metadata() instanceof QuerierMetadata querierMetadata) {
                    if (processActivatorAggregate.flows().size() != 1) {
                        System.err.println("Querier process must only have one input");
                        continue;
                    }
                    Map.Entry<TraverseName, FlowThroughAggregate> flowThroughEntry = processActivatorAggregate.flowsMap().entrySet().stream().findFirst().orElseThrow();
                    TraverseName traverseName = flowThroughEntry.getKey();
                    FlowThroughAggregate flow = flowThroughEntry.getValue();

                    if (flow.queries().size() != 1 && flow.inputs().size() == 0) {
                        System.err.println("Querier must only have one input from database");
                        continue;
                    }

                    DatabaseActivatorAggregate databaseActivatorAggregate = (DatabaseActivatorAggregate) getActivatorAggregate(querierMetadata.database(), domain.activators());
                    ProcessActivatorAggregate placeForQueryDefinition = (ProcessActivatorAggregate) getActivatorAggregate(querierMetadata.process(), domain.activators());

                    flow.moveQueryInputDefinitionTo(databaseActivatorAggregate, placeForQueryDefinition.flow(traverseName), null);
                } else if (processActivatorAggregate.metadata() instanceof LimitMetadata limitMetadata) {
                    if (processActivatorAggregate.flows().size() != 1) {
                        System.err.println("Limit can only have one flow");
                        continue;
                    }

                    Map.Entry<TraverseName, FlowThroughAggregate> flowThroughEntry = processActivatorAggregate.flowsMap().entrySet().stream().findFirst().orElseThrow();
                    TraverseName traverseName = flowThroughEntry.getKey();
                    FlowThroughAggregate flow = flowThroughEntry.getValue();

                    ActivatorAggregate sourceActivator = getActivatorAggregate(limitMetadata.dataSourceActivator(), domain.activators());
                    QualifiedName sourceQualifiedName = null;
                    if (sourceActivator instanceof ProcessActivatorAggregate p) {
                        sourceQualifiedName = QualifiedName.of(p.getOutput(traverseName), true);
                    } else if (sourceActivator instanceof ExternalEntityActivatorAggregate e) {
                        sourceQualifiedName = QualifiedName.of(e.starts().get(traverseName).stream().filter(connector -> !isPolicyConnector(connector, processingEnvironment)).findFirst().orElseThrow(), true);
                    }

                    if (sourceQualifiedName == null) {
                        throw new IllegalStateException();
                    }

                    flow.setOutputType(QualifiedName.of(Predicate.class.getName(), List.of(sourceQualifiedName)), false);
                } else if (processActivatorAggregate.metadata() instanceof GuardMetadata guardMetadata) {
                    if (processActivatorAggregate.flows().size() != 1) {
                        System.err.println("Guard can only have one flow; size: " + processActivatorAggregate.flows().size() +  " - " + processActivatorAggregate.flows().stream().map(FlowThroughAggregate::toString).collect(Collectors.joining()));
                        continue;
                    }

                    Map.Entry<TraverseName, FlowThroughAggregate> flowThroughEntry = processActivatorAggregate.flowsMap().entrySet().stream().findFirst().orElseThrow();
                    TraverseName traverseName = flowThroughEntry.getKey();
                    FlowThroughAggregate flow = flowThroughEntry.getValue();

                    ActivatorAggregate sourceActivator = getActivatorAggregate(guardMetadata.dataSourceActivator(), domain.activators());
                    QualifiedName sourceQualifiedName = null;
                    if (sourceActivator instanceof ProcessActivatorAggregate p) {
                        sourceQualifiedName = QualifiedName.of(p.getOutput(traverseName));
                    } else if (sourceActivator instanceof ExternalEntityActivatorAggregate e) {
                        sourceQualifiedName = QualifiedName.of(e.starts().get(traverseName).stream().filter(connector -> !isPolicyConnector(connector, processingEnvironment)).findFirst().orElseThrow());
                    }

                    if (sourceQualifiedName == null) {
                        throw new IllegalStateException();
                    }

                    flow.setOutputType(sourceQualifiedName, false);
                }
            }
        }

    }

    private static boolean isPolicyConnector(Connector connector, ProcessingEnvironment processingEnvironment) {
        var outputElement = processingEnvironment.getElementUtils().getTypeElement(connector.flowOutput().type().value());
        if (outputElement != null) {
            return outputElement.getInterfaces().stream().anyMatch(typeMirror -> typeMirror.toString().equals(Policy.class.getName()));
        }
        return false;
    }

    private static ActivatorAggregate getActivatorAggregate(ActivatorId activatorId, List<ActivatorAggregate> activatorAggregates) {
        return activatorAggregates.stream().filter(activatorAggregate -> activatorAggregate.id().equals(activatorId)).findAny()
                .orElseThrow(() -> new IllegalArgumentException("Expected to find activator id by " + activatorId + " from " + activatorAggregates.stream().map(ActivatorAggregate::toString).collect(Collectors.joining(", "))));
    }


}
