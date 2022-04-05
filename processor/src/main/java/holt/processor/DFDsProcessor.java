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
import static holt.processor.DFDParser.loadDfd;

public class DFDsProcessor extends AbstractProcessor {

    public static final String PACKAGE_NAME = "holt.processor.generation";
    public static final String EXTERNAL_ENTITY_PREFIX = "Abstract";
    public static final String PROCESS_SUFFIX = "Requirements";
    public static final String DATABASE_SUFFIX = "Requirements";

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                DFD.class.getName(),
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
        ConvertersResult convertersResult = getConverters(environment);

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
                        "Is your class name the same name as in the DFD?");
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

    private ConvertersResult getConverters(RoundEnvironment environment) {
        List<DFDToJavaFileConverter> converters = new ArrayList<>();
        List<ActivatorAggregate> allActivatorAggregates = new ArrayList<>();
        Map<ActivatorName, DFDName> activatorToDFDMap = new HashMap<>();
        Map<DFDName, Map<TraverseName, List<ActivatorAggregate>>> flows = new HashMap<>();
        Map<Element, ActivatorAggregate> elementToActivatorAggregateMap = new HashMap<>();

        for (var dfdPair : getRepeatableAnnotations(environment, DFD.class, DFDs.class, DFDs::value)) {
            DFD dfdAnnotation = dfdPair.annotation;
            DFDParser.DFD dfd = loadDfd(toInputStream(dfdAnnotation.csv()));

            DFDName dfdName = new DFDName(dfdAnnotation.name());

            Map<Integer, ActivatorAggregate> idToActivator = new HashMap<>();

            dfd.processes().forEach(node -> idToActivator.put(node.id(), new ProcessActivatorAggregate(new ActivatorName(node.name()))));
            dfd.databases().forEach(node -> idToActivator.put(node.id(), new DatabaseActivatorAggregate(new ActivatorName(node.name()))));
            dfd.externalEntities().forEach(node -> idToActivator.put(node.id(), new ExternalEntityActivatorAggregate(new ActivatorName(node.name()))));

            flows.put(dfdName, new HashMap<>());

            System.out.println("DFD is " + dfdName);
            Map<String, List<Dataflow>> dfdTraverseMap = createDFDFlowMap(dfd, environment);



            for (Map.Entry<String, List<Dataflow>> a : dfdTraverseMap.entrySet() ) {
                System.out.println("\tFlow: " + a.getKey());
                System.out.println("\t\t " + a.getValue().toString());
            }


            for (Map.Entry<String, List<Dataflow>> entry : dfdTraverseMap.entrySet()) {
                TraverseName traverseName = new TraverseName(entry.getKey());

                flows.get(dfdName).put(traverseName, new ArrayList<>());

                // Create Flows
                for (Dataflow dataflow : entry.getValue()) {
                    ActivatorAggregate from = idToActivator.get(dataflow.from().id());

                    // This is used to generate code. Databases are not needed,
                    // since each processor has a reference to all relevant
                    // databases through Connector
                    if (!(from instanceof DatabaseActivatorAggregate)) {
                        flows.get(dfdName).get(traverseName).add(from);
                    }

                    // Add to traverses
                    if (from instanceof ProcessActivatorAggregate fromProcessActivator) {
                        fromProcessActivator.addFlow(traverseName);
                    } else if (from instanceof ExternalEntityActivatorAggregate fromExternalEntityActivator) {
                        fromExternalEntityActivator.addStartFlow(traverseName);
                    }
                }

                // Adds the last Activator to traverses for the flowName, before it just adds the "to" for each dataflow
                int dataflows = entry.getValue().size();
                ActivatorAggregate lastActivatorAggregate = idToActivator.get(entry.getValue().get(dataflows - 1).to().id());
                flows.get(dfdName).get(traverseName).add(lastActivatorAggregate);

                // Connect Flows as inputs
                for (Dataflow dataflow : entry.getValue()) {
                    ActivatorAggregate fromActivatorAggregate = idToActivator.get(dataflow.from().id());
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

            List<DatabaseActivatorAggregate> databaseActivators = new ArrayList<>();
            List<ProcessActivatorAggregate> processActivators = new ArrayList<>();
            List<ExternalEntityActivatorAggregate> externalEntities = new ArrayList<>();

            allActivatorAggregates.addAll(idToActivator.values());
            idToActivator.values().forEach(activator -> activatorToDFDMap.put(activator.name(), dfdName));

            idToActivator.values().forEach(activator -> {
                if (activator instanceof DatabaseActivatorAggregate databaseActivator) {
                    databaseActivators.add(databaseActivator);
                } else if (activator instanceof ExternalEntityActivatorAggregate externalEntityActivator) {
                    externalEntities.add(externalEntityActivator);
                } else if (activator instanceof ProcessActivatorAggregate processActivator) {
                    processActivators.add(processActivator);
                }
            });

            converters.add(
                    new DFDToJavaFileConverter(
                            dfdName,
                            new Activators(
                                    databaseActivators,
                                    externalEntities,
                                    processActivators,
                                    flows.get(dfdName)
                            )
                    )
            );
        }


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

        // Not all activator aggregate have a class connected to them
        if (elementToActivatorAggregateMap.size() != allActivatorAggregates.size()) {
            System.out.println("************");
            System.out.println("* Warning, the following activators from the DFD does not have a class connected to them:");
            allActivatorAggregates.stream()
                    .filter(activatorAggregate -> activatorAggregate.qualifiedName().isEmpty())
                    .forEach(activatorAggregate -> System.out.println("* - " + activatorAggregate.name()));
            System.out.println("************");
        }

        return new ConvertersResult(
                converters,
                allActivatorAggregates,
                activatorToDFDMap,
                elementToActivatorAggregateMap
        );
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
     * This works since gradle copies csv files to class output.
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

    private Map<String, List<Dataflow>> createDFDFlowMap(DFDParser.DFD dfd, RoundEnvironment environment) {
        Map<String, List<Dataflow>> traverses = new HashMap<>();

        for (AnnotationWithTypeElement<Traverse> traversePair : getRepeatableAnnotations(environment, Traverse.class, Traverses.class, Traverses::value)) {
            Traverse annotation = traversePair.annotation;

            // name for the traverse
            String traverseName = annotation.name();
            // list of unique names for the flows in this traverse, in order
            String[] flowOrder = annotation.order();

            // check if the flow names are for this dfd
            boolean correctDFD = true;
            for (String flowName : flowOrder) {
                if(dfd.dataflows().get(flowName) == null) {
                    // data flow was not in dfd
                    correctDFD = false;
                }
            }

            if (!correctDFD) {
                continue;
            }

            // Add flows
            traverses.put(traverseName, new ArrayList<>());

            for (String flowName : flowOrder) {
                Dataflow dataflow = dfd.dataflows().get(flowName);

                if (dataflow == null) {
                    throw new IllegalStateException("No dataflow for id " + flowName);
                }

                traverses.get(traverseName).add(dataflow);
            }
        }

        return traverses;
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

