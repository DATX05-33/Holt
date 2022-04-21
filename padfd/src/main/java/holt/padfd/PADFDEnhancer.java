package holt.padfd;

import holt.JavaFileGenerator;
import holt.PrivacyActivatorJavaFileGenerator;
import holt.activator.ActivatorAggregate;
import holt.activator.ActivatorId;
import holt.activator.Connector;
import holt.activator.DatabaseActivatorAggregate;
import holt.activator.Domain;
import holt.activator.ExternalEntityActivatorAggregate;
import holt.activator.FlowThroughAggregate;
import holt.activator.ProcessActivatorAggregate;
import holt.activator.QualifiedName;
import holt.activator.TraverseName;
import holt.padfd.metadata.CombineMetadata;
import holt.padfd.metadata.LimitMetadata;
import holt.padfd.metadata.QuerierMetadata;
import holt.padfd.metadata.RequestMetadata;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PADFDEnhancer {

    private PADFDEnhancer() {}

    public static void enhance(Domain domain, ProcessingEnvironment processingEnvironment) {
        String dfdPackageName = JavaFileGenerator.packageOf(domain);
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

            activatorAggregates.forEach(activatorAggregate -> {
                if (activatorAggregate instanceof ProcessActivatorAggregate processActivatorAggregate) {
                    FlowThroughAggregate flow = processActivatorAggregate.flow(traverseName);
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
            });
        });

        /*
         *
         */
        for (ActivatorAggregate activator : domain.activators()) {
            if (activator instanceof ProcessActivatorAggregate processActivatorAggregate) {
                if (activator.metadata() instanceof CombineMetadata) {
                    PrivacyActivatorJavaFileGenerator.generateCombine(processActivatorAggregate, processingEnvironment, dfdPackageName);
                } else if (activator.metadata() instanceof QuerierMetadata querierMetadata) {
                    if (processActivatorAggregate.flows().size() != 1) {
                        throw new IllegalStateException("Querier process must only have one input");
                    }
                    Map.Entry<TraverseName, FlowThroughAggregate> flowThroughEntry = processActivatorAggregate.flowsMap().entrySet().stream().findFirst().orElseThrow();
                    TraverseName traverseName = flowThroughEntry.getKey();
                    FlowThroughAggregate flow = flowThroughEntry.getValue();

                    // Move the query definition
                    DatabaseActivatorAggregate databaseActivatorAggregate = (DatabaseActivatorAggregate) getActivatorAggregate(querierMetadata.database(), domain.activators());
                    ProcessActivatorAggregate placeForQueryDefinition = (ProcessActivatorAggregate) getActivatorAggregate(querierMetadata.process(), domain.activators());



                    flow.moveQueryInputDefinitionTo(databaseActivatorAggregate, placeForQueryDefinition.flow(traverseName), null);


                    PrivacyActivatorJavaFileGenerator.generateQuerier(processActivatorAggregate, processingEnvironment, dfdPackageName);
                } else if (activator.metadata() instanceof LimitMetadata) {
                    if (processActivatorAggregate.flows().size() != 1) {
                        throw new IllegalStateException("Limit can only have one flow");
                    }
                    FlowThroughAggregate flow = processActivatorAggregate.flows().get(0);

                    flow.setOutputType(new QualifiedName("java.lang.Boolean"), false);
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
