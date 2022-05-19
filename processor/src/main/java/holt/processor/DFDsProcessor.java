package holt.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import holt.processor.activator.*;
import holt.processor.annotation.*;
import holt.processor.annotation.representation.FlowThroughRep;
import holt.processor.annotation.representation.QueriesForRep;
import holt.processor.annotation.representation.TraverseRep;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static holt.processor.AnnotationValueUtils.getAnnotationClassValue;

public class DFDsProcessor extends AbstractProcessor {

    public static final String PACKAGE_NAME = "holt.processor.generation";
    public static final String EXTERNAL_ENTITY_PREFIX = "Abstract";
    public static final String PROCESS_SUFFIX = "Requirements";
    public static final String DATABASE_SUFFIX = "Requirements";

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
        ConvertersResult convertersResult = null;
        try {
            convertersResult = getConverters(environment);
        } catch (DFDParser.NotWellFormedDFDException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }

        // Annotations that will be applied to the converters
        Map<DFDName, List<TraverseRep>> transverseRepMap = getTraverses(convertersResult, environment);
        Map<DFDName, List<FlowThroughRep>> flowThroughRepMap = getFlowThroughs(convertersResult, environment);
        Map<DFDName, List<QueriesForRep>> queriesForRepMap = getQueriesFor(convertersResult, environment);

        for (DFDToJavaFileConverter converter : convertersResult.converters) {
            DFDName dfdName = converter.getDFDName();

            // Apply annotations to dfd
            if (transverseRepMap.containsKey(dfdName)) {
                converter.applyTraverses(transverseRepMap.get(dfdName));
            }

            if (flowThroughRepMap.containsKey(dfdName)) {
                converter.applyFlowThrough(flowThroughRepMap.get(dfdName));
            }

            if (queriesForRepMap.containsKey(dfdName)) {
                converter.applyQueriesFor(queriesForRepMap.get(dfdName));
            }

            // Convert to java files for each dfd
            saveJavaFiles(converter.convertToJavaFiles());
        }

        return true;
    }

    private static final class ConvertersResult {
        private final List<DFDToJavaFileConverter> converters;
        private final List<ActivatorAggregate> activatorAggregates;
        private final Map<ActivatorName, DFDName> activatorToDFDMap;
        private final Map<Element, ActivatorAggregate> elementToActivatorAggregate;

        private ConvertersResult(List<DFDToJavaFileConverter> converters,
                                 List<ActivatorAggregate> activatorAggregates,
                                 Map<ActivatorName, DFDName> activatorToDFDMap,
                                 Map<Element, ActivatorAggregate> elementToActivatorAggregate) {
            this.converters = converters;
            this.activatorAggregates = activatorAggregates;
            this.activatorToDFDMap = activatorToDFDMap;
            this.elementToActivatorAggregate = elementToActivatorAggregate;
        }

        public ActivatorAggregate getActivatorAggregate(Element element) {
            ActivatorAggregate activatorAggregate = elementToActivatorAggregate.get(element);
            if (activatorAggregate == null) {
                throw new IllegalStateException("Cannot find an activator aggregate for element: " +
                        element.toString() +
                        ", have you annotated the class with @Activator yet? " +
                        "Is your class name the same name as in the DFD?\n" +
                        "Registered activators: " +
                        elementToActivatorAggregate.keySet());
            }
            return activatorAggregate;
        }

        public ActivatorAggregate getActivatorAggregateByClassName(ClassName className) {
            List<ActivatorAggregate> activatorAggregates = elementToActivatorAggregate
                    .values()
                    .stream()
                    .filter(activatorAggregate -> activatorAggregate.name().value().equals(className.simpleName().toString()))
                    .toList();

            if (activatorAggregates.size() != 1) {
                throw new IllegalStateException("Could not find a suiting ActivatorAggregate with the ClassName: " + className.simpleName());
            }

            return activatorAggregates.get(0);
        }

    }

    private ConvertersResult getConverters(RoundEnvironment environment) throws DFDParser.NotWellFormedDFDException {
        List<DFDToJavaFileConverter> converters = new ArrayList<>();
        List<ActivatorAggregate> allActivatorAggregates = new ArrayList<>();
        Map<Element, ActivatorAggregate> elementToActivatorAggregateMap = new HashMap<>();
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
                orderedDFD = PADFDEnhancer.enhance(orderedDFD);
            }

            orderedDFD.processes().forEach(node -> idToActivator.put(node.id(), new ProcessActivatorAggregate(new ActivatorName(node.name()))));
            orderedDFD.databases().forEach(node -> idToActivator.put(node.id(), new DatabaseActivatorAggregate(new ActivatorName(node.name()))));
            orderedDFD.externalEntities().forEach(node -> idToActivator.put(node.id(), new ExternalEntityActivatorAggregate(new ActivatorName(node.name()))));

            allActivatorAggregates.addAll(idToActivator.values());
            idToActivator.values().forEach(activator -> activatorToDFDMap.put(activator.name(), dfdName));

            elementToActivatorAggregateMap.putAll(
                    connectAggregatesWithActivatorAnnotation(environment, idToActivator.values())
            );

            traverses.put(
                    dfdName,
                    connectAggregatesForDFD(orderedDFD, idToActivator)
            );

            converters.add(
                        new DFDToJavaFileConverter(
                                dfdName,
                                new Activators(
                                        new ArrayList<>(idToActivator.values()),
                                        traverses.get(dfdName)
                                )
                        )
                );
        }

        logUnannotatedActivators(elementToActivatorAggregateMap, allActivatorAggregates);

        return new ConvertersResult(
                converters,
                allActivatorAggregates,
                activatorToDFDMap,
                elementToActivatorAggregateMap
        );
    }

    private Map<Element, ActivatorAggregate> connectAggregatesWithActivatorAnnotation(RoundEnvironment environment, Collection<ActivatorAggregate> allActivatorAggregates) {
        Map<Element, ActivatorAggregate> elementToActivatorAggregateMap = new HashMap<>();

        for (Element element : environment.getElementsAnnotatedWith(Activator.class)) {
            Activator activator = element.getAnnotation(Activator.class);

            if (element instanceof TypeElement typeElement) {
                String activatorFromGraphName = activator.graphName().equals("")
                        ? typeElement.getSimpleName().toString()
                        : activator.graphName();
                for (ActivatorAggregate activatorAggregate : allActivatorAggregates) {
                    if (activatorFromGraphName.equals(activatorAggregate.name().value())) {
                        elementToActivatorAggregateMap.put(element, activatorAggregate);
                        activatorAggregate.setQualifiedName(new QualifiedName(typeElement.getQualifiedName().toString()));
                        break;
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
                if (!(from instanceof DatabaseActivatorAggregate)) {
                    if (!order.contains(from)) {
                        order.add(from);
                    }
                }

                // Add to traverses
                if (from instanceof ProcessActivatorAggregate fromProcessActivator) {
                    fromProcessActivator.addFlow(traverseName);
                } else if (from instanceof ExternalEntityActivatorAggregate fromExternalEntityActivator) {
                    fromExternalEntityActivator.addStartFlow(traverseName);
                }

                if (to instanceof ExternalEntityActivatorAggregate || to instanceof DatabaseActivatorAggregate) {
                    // If the last is not the same activator. This is done to make sure Joins work.
                    if (!order.get(order.size() - 1).equals(to)) {
                        order.add(to);
                    }
                }
            }

            // Connect Flows as inputs
            for (DFDRep.Flow dataflow : entry.getValue()) {
                ActivatorAggregate fromActivatorAggregate = idToActivator.get(dataflow.from().id());
                Objects.requireNonNull(fromActivatorAggregate);

                Connector connector = null;
                if (fromActivatorAggregate instanceof ExternalEntityActivatorAggregate externalEntityActivator) {
                    connector = externalEntityActivator.getOutput(traverseName);
                } else if (fromActivatorAggregate instanceof ProcessActivatorAggregate processActivator) {
                    connector = processActivator.getOutput(traverseName);
                } else if (fromActivatorAggregate instanceof DatabaseActivatorAggregate databaseActivator) {
                    connector = new QueryConnector(databaseActivator);
                }

                ActivatorAggregate toActivatorAggregate = idToActivator.get(dataflow.to().id());
                if (toActivatorAggregate instanceof ProcessActivatorAggregate toProcessActivator) {
                    toProcessActivator.addInputToFlow(traverseName, connector);
                } else if (toActivatorAggregate instanceof ExternalEntityActivatorAggregate externalEntityActivator) {
                    externalEntityActivator.addEnd(traverseName, connector);
                } else if (toActivatorAggregate instanceof DatabaseActivatorAggregate databaseActivator) {
                    databaseActivator.addStore(traverseName, connector);
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
                    .filter(activatorAggregate -> activatorAggregate.qualifiedName().isEmpty())
                    .forEach(activatorAggregate -> System.out.println("* - " + activatorAggregate.name()));
            System.out.println("************");
        } else {
            System.out.println("All activators have been annotated properly");
        }
    }

    private Map<DFDName, List<FlowThroughRep>> getFlowThroughs(ConvertersResult convertersResult,
                                                               RoundEnvironment environment) {
        Map<ActivatorName, DFDName> activatorToDFDMap = convertersResult.activatorToDFDMap;

        Map<DFDName, List<FlowThroughRep>> dfdToFlowThroughRepMap = new HashMap<>();
        for (var flowThroughPair : getRepeatableAnnotations(environment, FlowThrough.class, FlowThroughs.class, FlowThroughs::value)) {
            FlowThrough flowThrough = flowThroughPair.annotation;
            TypeElement typeElement = flowThroughPair.typeElement;

            ProcessActivatorAggregate processActivator = (ProcessActivatorAggregate) convertersResult.getActivatorAggregate(typeElement);
            processActivator.setQualifiedName(new QualifiedName(typeElement.getQualifiedName().toString()));

            DFDName dfdName = activatorToDFDMap.get(processActivator.name());

            if (!dfdToFlowThroughRepMap.containsKey(dfdName)) {
                dfdToFlowThroughRepMap.put(dfdName, new ArrayList<>());
            }

            dfdToFlowThroughRepMap
                    .get(dfdName)
                    .add(FlowThroughRep
                            .of(processActivator, flowThrough)
                            .with(this)
                    );
        }

        return dfdToFlowThroughRepMap;
    }

    private Map<DFDName, List<QueriesForRep>> getQueriesFor(ConvertersResult convertersResult,
                                                            RoundEnvironment environment) {
        Map<DFDName, List<QueriesForRep>> queriesForRepMap = new HashMap<>();
        var activatorToDFDMap = convertersResult.activatorToDFDMap;

        for (Element element : environment.getElementsAnnotatedWith(QueriesFor.class)) {
            TypeElement typeElement = (TypeElement) element;
            QueriesFor queriesFor = element.getAnnotation(QueriesFor.class);
            ClassName queriesForClassName = ClassName.bestGuess(
                    getAnnotationClassValue(
                            this, queriesFor, QueriesFor::value
                    ).toString()
            );

            DatabaseActivatorAggregate databaseActivatorAggregate = (DatabaseActivatorAggregate) convertersResult.getActivatorAggregateByClassName(queriesForClassName);
            QueriesForRep queriesForRep = new QueriesForRep(databaseActivatorAggregate, ClassName.bestGuess(typeElement.getQualifiedName().toString()));

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

    private void saveJavaFiles(List<JavaFile> javaFiles) {
        javaFiles.forEach(javaFile -> {
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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

    private Map<DFDName, List<TraverseRep>> getTraverses(ConvertersResult convertersResult, RoundEnvironment environment) {
        Map<ActivatorName, DFDName> activatorToDFDMap = convertersResult.activatorToDFDMap;

        Map<DFDName, List<TraverseRep>> dfdTraverseRepMap = new HashMap<>();
        for (var traversePair : getRepeatableAnnotations(environment, Traverse.class, Traverses.class, Traverses::value)) {
            Traverse annotation = traversePair.annotation;
            TypeElement typeElement = traversePair.typeElement;

            ExternalEntityActivatorAggregate externalEntityActivator = (ExternalEntityActivatorAggregate) convertersResult.getActivatorAggregate(typeElement);
            externalEntityActivator.setQualifiedName(new QualifiedName(typeElement.getQualifiedName().toString()));

            DFDName dfdName = activatorToDFDMap.get(externalEntityActivator.name());
            if (!dfdTraverseRepMap.containsKey(dfdName)) {
                dfdTraverseRepMap.put(dfdName, new ArrayList<>());
            }

            dfdTraverseRepMap
                    .get(dfdName)
                    .add(
                            TraverseRep
                                    .of(annotation, externalEntityActivator)
                                    .with(this)
                    );
        }

        return dfdTraverseRepMap;
    }
}

