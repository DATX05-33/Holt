package holt.processor;

import holt.DFDOrderedRep;
import holt.DFDRep;
import holt.JavaFileGenerator;
import holt.activator.FlowThroughAggregate;
import holt.padfd.PADFDEnhancer;
import holt.activator.ActivatorAggregate;
import holt.activator.ActivatorId;
import holt.activator.ActivatorName;
import holt.activator.ConnectedClass;
import holt.activator.Connector;
import holt.activator.DFDName;
import holt.activator.DatabaseActivatorAggregate;
import holt.activator.Domain;
import holt.activator.ExternalEntityActivatorAggregate;
import holt.activator.OutputActivator;
import holt.activator.ProcessActivatorAggregate;
import holt.activator.QualifiedName;
import holt.activator.TraverseName;
import holt.applier.FlowThroughApplier;
import holt.applier.FlowThroughRep;
import holt.applier.QueriesForApplier;
import holt.applier.QueriesForRep;
import holt.applier.TraverseApplier;
import holt.applier.TraverseRep;
import holt.padfd.PADFDTransformater;
import holt.parser.DFDParser;
import holt.processor.annotation.Activator;
import holt.processor.annotation.DFD;
import holt.processor.annotation.DFDs;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.FlowThroughs;
import holt.processor.annotation.QueriesFor;
import holt.processor.annotation.Traverse;
import holt.processor.annotation.Traverses;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static holt.processor.AnnotationValueUtils.getAnnotationClassValue;

public class DFDsProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                DFDRep.class.getName(),
                DFDs.class.getName(),
                Activator.class.getName(),
                FlowThrough.class.getName(),
                FlowThroughs.class.getName(),
                QueriesFor.class.getName(),
                Traverse.class.getName(),
                Traverses.class.getName()
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment) {
        if (environment.processingOver()) {
            return true;
        }

        // Converters that will convert DFD to Java Files
        ProcessorResults processorResults;
        try {
            processorResults = readBaseAnnotations(environment);
        } catch (DFDParser.NotWellFormedDFDException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }

        // Annotations that will be applied to the converters
        Map<DFDName, List<TraverseRep>> transverseRepMap = getTraverses(processorResults, environment);
        Map<DFDName, List<FlowThroughRep>> flowThroughRepMap = getFlowThroughs(processorResults, environment);
        Map<DFDName, List<QueriesForRep>> queriesForRepMap = getQueriesFor(processorResults, environment);

        for (Domain domain : processorResults.domains) {
            DFDName dfdName = domain.name();

            // Apply annotations to dfd
            if (transverseRepMap.containsKey(dfdName)) {
                TraverseApplier.applyTraverseRep(transverseRepMap.get(dfdName));
            }

            if (flowThroughRepMap.containsKey(dfdName)) {
                FlowThroughApplier.applyFlowThrough(flowThroughRepMap.get(dfdName));
            }

            if (queriesForRepMap.containsKey(dfdName)) {
                QueriesForApplier.applyQueriesFor(queriesForRepMap.get(dfdName));
            }

            boolean validExternalEntities = domain.externalEntities()
                    .map(activatorAggregates -> activatorAggregates.connectedClass().isPresent())
                    .reduce(true, (b1, b2) -> b1 && b2);

            if (validExternalEntities && domain.privacyAware()) {
                PADFDEnhancer.enhance(domain, processingEnv);
            }

            domain.processes().forEach(processActivatorAggregate ->
                    processActivatorAggregate.flows().forEach(FlowThroughAggregate::runLaterQuerySetup)
            );

            //TODO:
//            logUnannotatedActivators(elementToActivatorAggregateMap, allActivatorAggregates);

            JavaFileGenerator.saveJavaFiles(domain, processingEnv, validExternalEntities);
        }

        return true;
    }

    public static final class ProcessorResults {
        private final List<Domain> domains;
        private final Map<ActivatorName, DFDName> activatorToDFDMap;
        private final Map<Element, List<ActivatorAggregate>> elementToActivatorAggregate;

        public ProcessorResults(List<Domain> domains,
                                Map<ActivatorName, DFDName> activatorToDFDMap,
                                Map<Element, List<ActivatorAggregate>> elementToActivatorAggregate) {
            this.domains = domains;
            this.activatorToDFDMap = activatorToDFDMap;
            this.elementToActivatorAggregate = elementToActivatorAggregate;
        }

        public List<ActivatorAggregate> getActivatorAggregate(Element element) {
            List<ActivatorAggregate> activatorAggregates = elementToActivatorAggregate.get(element);
            if (activatorAggregates == null) {
                throw new IllegalStateException("Cannot find an activator aggregate for element: " +
                        element.toString() +
                        ", have you annotated the class with @Activator yet? " +
                        "Is your class name the same name as in the DFD?\n" +
                        "Registered activators: " +
                        elementToActivatorAggregate.keySet());
            }
            return activatorAggregates;
        }

        public ActivatorAggregate getActivatorAggregateForceOne(Element element) {
            List<ActivatorAggregate> activatorAggregates = elementToActivatorAggregate.get(element);
            if (activatorAggregates == null) {
                throw new IllegalStateException("Cannot find an activator aggregate for element: " +
                        element.toString() +
                        ", have you annotated the class with @Activator yet? " +
                        "Is your class name the same name as in the DFD?\n" +
                        "Registered activators: " +
                        elementToActivatorAggregate.keySet());
            } else if (activatorAggregates.size() != 1) {
                throw new IllegalStateException("Must only be one activator aggregate");
            }
            return activatorAggregates.get(0);
        }


        public ActivatorAggregate getActivatorAggregateByClassName(QualifiedName qualifiedName) {
            List<ActivatorAggregate> activatorAggregates = elementToActivatorAggregate
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(activatorAggregate -> activatorAggregate.name().value().equals(qualifiedName.simpleName()))
                    .toList();

            if (activatorAggregates.size() != 1) {
                throw new IllegalStateException("Could not find one suiting ActivatorAggregate with the ClassName: " + qualifiedName.simpleName() +
                        "\n Available: " + elementToActivatorAggregate.values().stream().flatMap(Collection::stream).map(ActivatorAggregate::name).map(ActivatorName::toString).collect(Collectors.joining(", ")));
            }

            return activatorAggregates.get(0);
        }

    }

    private ProcessorResults readBaseAnnotations(RoundEnvironment environment) throws DFDParser.NotWellFormedDFDException {
        List<Domain> domains = new ArrayList<>();
        Map<Element, List<ActivatorAggregate>> elementToActivatorAggregate = new HashMap<>();
        Map<ActivatorName, DFDName> activatorToDFDMap = new HashMap<>();
        Map<DFDName, Map<TraverseName, List<ActivatorAggregate>>> traverses = new HashMap<>();

        record DFDData (DFDRep dfd, boolean privacyAware) { }

        Map<DFDName, DFDData> dfdMap = new HashMap<>();
        for (var dfdPair : getRepeatableAnnotations(environment, DFD.class, DFDs.class, DFDs::value)) {
            DFD dfdAnnotation = dfdPair.annotation;
            InputStream inputStream = toInputStream(dfdAnnotation.xml());
            var dfd = DFDParser.fromDrawIO(inputStream);
            DFDName dfdName = new DFDName(dfdAnnotation.name());

            dfdMap.put(dfdName, new DFDData(dfd, dfdAnnotation.privacyAware()));
        }

        for (var dfdEntry : dfdMap.entrySet()) {
            var dfdName = dfdEntry.getKey();
            var dfdData = dfdEntry.getValue();
            var dfd = dfdData.dfd;
            // Ugh we don't have the privacy aware activators
            Map<String, ActivatorAggregate> idToActivator = new HashMap<>();

            var orderedDFD = DFDParser.fromDrawIO(dfd, getDFDTraverseOrders(dfd, environment));

            if (dfdData.privacyAware) {
                orderedDFD = PADFDTransformater.enhance(orderedDFD);
            }

            orderedDFD.processes().forEach(node -> idToActivator.put(node.id(), new ProcessActivatorAggregate(new ActivatorId(node.id()), new ActivatorName(node.name()), node.metadata())));
            orderedDFD.databases().forEach(node -> idToActivator.put(node.id(), new DatabaseActivatorAggregate(new ActivatorId(node.id()), new ActivatorName(node.name()), node.metadata())));
            orderedDFD.externalEntities().forEach(node -> idToActivator.put(node.id(), new ExternalEntityActivatorAggregate(new ActivatorId(node.id()), new ActivatorName(node.name()), node.metadata())));

            if (orderedDFD.activators().size() != idToActivator.size()) {
                throw new IllegalStateException("One or more of the ids are not unique");
            }

            elementToActivatorAggregate.putAll(
                    connectAggregatesWithActivatorAnnotation(environment, idToActivator.values())
            );

            idToActivator.values().forEach(activator -> activatorToDFDMap.put(activator.name(), dfdName));

            traverses.put(
                    dfdName,
                    connectAggregatesForDFD(orderedDFD, idToActivator)
            );

            domains.add(
                    new Domain(
                            dfdName,
                            new ArrayList<>(idToActivator.values()),
                            traverses.get(dfdName),
                            dfdData.privacyAware
                    )
            );
        }

        return new ProcessorResults(
                domains,
                activatorToDFDMap,
                elementToActivatorAggregate
        );
    }

    private Map<Element, List<ActivatorAggregate>> connectAggregatesWithActivatorAnnotation(RoundEnvironment environment, Collection<ActivatorAggregate> allActivatorAggregates) {
        Map<Element, List<ActivatorAggregate>> elementToActivatorAggregateMap = new HashMap<>();

        for (Element element : environment.getElementsAnnotatedWith(Activator.class)) {
            Activator activator = element.getAnnotation(Activator.class);

            if (element instanceof TypeElement typeElement) {
                List<String> possible = new ArrayList<>();
                for (TypeMirror anInterface : typeElement.getInterfaces()) {
                    possible.add(anInterface.toString());
                }
                possible.add(typeElement.getSuperclass().toString());

                for (ActivatorAggregate activatorAggregate : allActivatorAggregates) {
                    for (String activatorRequirementsName : possible) {
                        if (activatorRequirementsName.equals(activatorAggregate.requirementsName().value())) {
                            if (!elementToActivatorAggregateMap.containsKey(element)) {
                                elementToActivatorAggregateMap.put(element, new ArrayList<>());
                            }
                            elementToActivatorAggregateMap.get(element).add(activatorAggregate);

                            activatorAggregate.setActivatorName(new ActivatorName(typeElement.getSimpleName().toString()));
                            activatorAggregate.setConnectedClass(
                                    new ConnectedClass(
                                            QualifiedName.of(typeElement.getQualifiedName().toString()),
                                            activator.instantiateWithReflection()
                                    )
                            );
                        }
                    }
                }
            }
        }

        return elementToActivatorAggregateMap;
    }

    private Map<TraverseName, List<ActivatorAggregate>> connectAggregatesForDFD(DFDOrderedRep orderedDFD, Map<String, ActivatorAggregate> idToActivator) {
        Map<TraverseName, List<ActivatorAggregate>> traverses = new HashMap<>();

        for (Map.Entry<String, List<DFDRep.Flow>> entry : orderedDFD.traverses().entrySet()) {
            TraverseName traverseName = new TraverseName(entry.getKey());
            List<ActivatorAggregate> order = new ArrayList<>();

            traverses.put(traverseName, order);

            // Create Flows
            for (DFDRep.Flow dataflow : entry.getValue()) {
                ActivatorAggregate from = idToActivator.get(dataflow.from().id());
                ActivatorAggregate to = idToActivator.get(dataflow.to().id());

                // This is used to generate code. Databases are not needed,
                // since each processor has a reference to all relevant
                // databases through Connector
                if (from instanceof ExternalEntityActivatorAggregate || from instanceof ProcessActivatorAggregate) {
                    if (!order.contains(from)) {
                        order.add(from);
                    }
                }

                // Add to traverses
                if (from instanceof ProcessActivatorAggregate fromProcessActivator) {
                    fromProcessActivator.createFlowThrough(traverseName);
                }

                if (to instanceof OutputActivator outputActivator) {
                    // if it is going back to the start, then it can only be added one more.
                    if (to.equals(order.get(0))) {
                        for (int i = 1; i < order.size(); i++) {
                            if (order.get(i).equals(to)) {
                                throw new IllegalStateException("Cannot add the start external entity more than once. "
                                        + "There's is just one value that can be returned.");
                            }
                        }
                        order.add(to);
                        outputActivator.addOutput(traverseName);
                    }
                    // If it already exists, move it the back to ensure it's called only when all inputs have been defined.
                    else if (order.contains(to)) {
                        order.remove(to);
                        order.add(to);
                    } else {
                        order.add(to);
                        outputActivator.addOutput(traverseName);
                    }
                }
            }

            // Connect Flows as inputs
            for (DFDRep.Flow dataflow : entry.getValue()) {
                ActivatorAggregate fromActivatorAggregate = idToActivator.get(dataflow.from().id());
                ActivatorAggregate toActivatorAggregate = idToActivator.get(dataflow.to().id());
                Objects.requireNonNull(fromActivatorAggregate);

                if (fromActivatorAggregate instanceof ProcessActivatorAggregate fromProcessActivator) {
                    Connector connector = fromProcessActivator.getOutput(traverseName);
                    if (toActivatorAggregate instanceof ProcessActivatorAggregate toProcessActivator) {
                        toProcessActivator.addInputToFlow(traverseName, connector);
                    } else if (toActivatorAggregate instanceof OutputActivator outputActivator) {
                        outputActivator.addInputToTraverseOutput(traverseName, connector);
                    }
                } else if (fromActivatorAggregate instanceof DatabaseActivatorAggregate fromDatabaseActivator) {
                    // If fromActivatorAggregate is a database, then the 'to' must be a Process.
                    ProcessActivatorAggregate toProcessActivator = (ProcessActivatorAggregate) toActivatorAggregate;
                    toProcessActivator.addQueryInputToFlow(traverseName, fromDatabaseActivator);
                } else if (fromActivatorAggregate instanceof ExternalEntityActivatorAggregate externalEntityActivatorAggregate) {
                    externalEntityActivatorAggregate.addLateConnector(traverseName, connectors -> {
                        if (toActivatorAggregate instanceof ProcessActivatorAggregate toProcessActivator) {
                            for (Connector connector : connectors) {
                                toProcessActivator.addInputToFlow(traverseName, connector);
                            }
                        } else if (toActivatorAggregate instanceof OutputActivator outputActivator) {
                            for (Connector connector : connectors) {
                                outputActivator.addInputToTraverseOutput(traverseName, connector);
                            }
                        }
                    });
                }
            }
        }

        return traverses;
    }

    private void logUnannotatedActivators(Map<Element, ActivatorAggregate> elementToActivatorAggregateMap, List<ActivatorAggregate> allActivatorAggregates) {
        // Not all activator aggregate have a class connected to them
        if (elementToActivatorAggregateMap.size() != allActivatorAggregates.size()) {
            System.out.println("************");
            System.out.println("* Warning, the following activators from a DFD does not have a class connected to them:");
            allActivatorAggregates.stream()
                    .filter(activatorAggregate -> activatorAggregate.connectedClass().isEmpty())
                    .forEach(activatorAggregate -> System.out.println("* - " + activatorAggregate.name()));
            System.out.println("************");
        } else {
            System.out.println("All activators have been annotated properly");
        }
    }

    private String temp(ActivatorAggregate activatorAggregate) {
        return activatorAggregate.requirementsName().value().replaceAll("Requirements", "");
    }

    private Map<DFDName, List<FlowThroughRep>> getFlowThroughs(ProcessorResults processorResults,
                                                               RoundEnvironment environment) {
        Map<ActivatorName, DFDName> activatorToDFDMap = processorResults.activatorToDFDMap;

        Map<DFDName, List<FlowThroughRep>> dfdToFlowThroughRepMap = new HashMap<>();
        for (var flowThroughPair : getRepeatableAnnotations(environment, FlowThrough.class, FlowThroughs.class, FlowThroughs::value)) {
            FlowThrough flowThrough = flowThroughPair.annotation;
            TypeElement typeElement = flowThroughPair.typeElement;

            ProcessActivatorAggregate processActivator = null;
            List<ActivatorAggregate> possibleActivatorAggregates = processorResults.getActivatorAggregate(typeElement);
            if (possibleActivatorAggregates.size() > 1 && flowThrough.forActivator().equals("")) {
                throw new IllegalArgumentException("Ambiguity!!");
            } else if (possibleActivatorAggregates.size() > 1) {
                for (ActivatorAggregate possibleActivatorAggregate : possibleActivatorAggregates) {
                    if (temp(possibleActivatorAggregate).equals(flowThrough.forActivator())) {
                        processActivator = (ProcessActivatorAggregate) possibleActivatorAggregate;
                        break;
                    }
                }

                if (processActivator == null) {
                    throw new IllegalArgumentException("Cannot find activator with name: "
                            + flowThrough.forActivator()
                            + " - Available: "
                            + possibleActivatorAggregates.stream().map(this::temp).collect(Collectors.joining(", ")));
                }

            } else {
                // Must be at least one. Otherwise, it would have thrown earlier.
                processActivator = (ProcessActivatorAggregate) possibleActivatorAggregates.get(0);
            }

            DFDName dfdName = activatorToDFDMap.get(processActivator.name());
            if (dfdName == null) {
                throw new IllegalStateException("DFDName cannot be null");
            }

            if (!dfdToFlowThroughRepMap.containsKey(dfdName)) {
                dfdToFlowThroughRepMap.put(dfdName, new ArrayList<>());
            }

            dfdToFlowThroughRepMap
                    .get(dfdName)
                    .add(
                            RepBuilder.createFlowThroughRep(
                                    processActivator,
                                    processorResults,
                                    flowThrough,
                                    this
                            )
                    );
        }

        return dfdToFlowThroughRepMap;
    }

    private Map<DFDName, List<QueriesForRep>> getQueriesFor(ProcessorResults processorResults,
                                                            RoundEnvironment environment) {
        Map<DFDName, List<QueriesForRep>> queriesForRepMap = new HashMap<>();
        var activatorToDFDMap = processorResults.activatorToDFDMap;

        for (Element element : environment.getElementsAnnotatedWith(QueriesFor.class)) {
            TypeElement typeElement = (TypeElement) element;
            QueriesFor queriesFor = element.getAnnotation(QueriesFor.class);
            QualifiedName queriesForClassName = QualifiedName.of(
                    getAnnotationClassValue(
                            this, queriesFor, QueriesFor::value
                    ).toString()
            );

            DatabaseActivatorAggregate databaseActivatorAggregate = (DatabaseActivatorAggregate) processorResults.getActivatorAggregateByClassName(queriesForClassName);
            QueriesForRep queriesForRep = new QueriesForRep(databaseActivatorAggregate, QualifiedName.of(typeElement.getQualifiedName().toString()));

            DFDName dfdName = activatorToDFDMap.get(databaseActivatorAggregate.name());

            if (!queriesForRepMap.containsKey(dfdName)) {
                queriesForRepMap.put(dfdName, new ArrayList<>());
            }

            queriesForRepMap.get(dfdName).add(queriesForRep);
        }

        return queriesForRepMap;
    }

    private record AnnotationWithTypeElement<S extends Annotation>(S annotation, TypeElement typeElement) {
        AnnotationWithTypeElement {
            Objects.requireNonNull(annotation);
            Objects.requireNonNull(typeElement);
        }
    }
    private <S extends Annotation, R extends Annotation> List<AnnotationWithTypeElement<S>> getRepeatableAnnotations(RoundEnvironment environment, Class<S> singleClass, Class<R> repeatableClass, Function<R,S[]> getSingles) {
        return Stream.of(environment.getElementsAnnotatedWith(singleClass), environment.getElementsAnnotatedWith(repeatableClass))
                .flatMap(Collection::stream)
                .filter(element -> element instanceof TypeElement)
                .map(element -> (TypeElement) element)
                .map(typeElement -> {
                    Stream<S> toStream;

                    R repeatableAnnotation = typeElement.getAnnotation(repeatableClass);
                    if (repeatableAnnotation != null) {
                        // Repeatable
                        S[] singles = getSingles.apply(repeatableAnnotation);
                        toStream = Stream.of(singles);
                    } else {
                        // Single
                        S single = typeElement.getAnnotation(singleClass);
                        toStream = Stream.of(single);
                    }

                    return toStream
                            .filter(Objects::nonNull)
                            .map(s -> new AnnotationWithTypeElement<>(s, typeElement))
                            .toList();
                })
                .flatMap(Collection::stream)
                .distinct()
                .toList();
    }

    /**
     * This works since gradle copies xml files to class output.
     */
    private InputStream toInputStream(String dfdFile) {
        try {
            return processingEnv.getFiler().getResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    dfdFile
            ).openInputStream();
        } catch (IOException e) {
            System.err.println("Error trying to read " + dfdFile + "; cannot find it");
        }

        return null;
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return this.processingEnv;
    }

    // Traverse -> List of Flow
    private Map<String, List<String>> getDFDTraverseOrders(DFDRep dfd, RoundEnvironment environment) {
        Map<String, List<String>> traverseOrder = new HashMap<>();

        for (AnnotationWithTypeElement<Traverse> traversePair : getRepeatableAnnotations(environment, Traverse.class, Traverses.class, Traverses::value)) {
            Traverse annotation = traversePair.annotation;

            // Check that the Traverse is a part of the DFD.
            boolean found = false;
            for (DFDRep.Flow flow : dfd.flows()) {
                for (String traverseFlow : annotation.order()) {
                    if (flow.id().equals(traverseFlow)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }

            if (!found) {
                continue;
            }

            // name for the traverse
            String traverseName = annotation.name();
            // list of unique names for the flows in this traverse, in order
            String[] flowOrder = annotation.order();

            // Add flows
            traverseOrder.put(traverseName, new ArrayList<>());

            for (String flowName : flowOrder) {
                if (flowName == null) {
                    throw new IllegalStateException("No dataflow for id " + flowName);
                }

                traverseOrder.get(traverseName).add(flowName);
            }
        }

        return traverseOrder;
    }

    private Map<DFDName, List<TraverseRep>> getTraverses(ProcessorResults processorResults, RoundEnvironment environment) {
        Map<ActivatorName, DFDName> activatorToDFDMap = processorResults.activatorToDFDMap;

        Map<DFDName, List<TraverseRep>> dfdTraverseRepMap = new HashMap<>();
        for (var traversePair : getRepeatableAnnotations(environment, Traverse.class, Traverses.class, Traverses::value)) {
            Traverse annotation = traversePair.annotation;
            TypeElement typeElement = traversePair.typeElement;

            ExternalEntityActivatorAggregate externalEntityActivator = (ExternalEntityActivatorAggregate) processorResults.getActivatorAggregateForceOne(typeElement);
            externalEntityActivator.setConnectedClass(
                    new ConnectedClass(
                            QualifiedName.of(typeElement.getQualifiedName().toString()),
                            false
                    )
            );

            DFDName dfdName = activatorToDFDMap.get(externalEntityActivator.name());
            if (!dfdTraverseRepMap.containsKey(dfdName)) {
                dfdTraverseRepMap.put(dfdName, new ArrayList<>());
            }

            dfdTraverseRepMap
                    .get(dfdName)
                    .add(
                            RepBuilder.createTraverseRep(
                                    annotation,
                                    externalEntityActivator,
                                    this
                            )
                    );
        }

        return dfdTraverseRepMap;
    }
}

