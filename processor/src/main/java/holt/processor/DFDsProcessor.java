package holt.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import holt.processor.activator.ActivatorName;
import holt.processor.activator.Connector;
import holt.processor.activator.QualifiedName;
import holt.processor.annotation.DFD;
import holt.processor.annotation.Database;
import holt.processor.annotation.FlowStart;
import holt.processor.annotation.representation.DatabaseRep;
import holt.processor.annotation.representation.FlowStartRep;
import holt.processor.annotation.FlowStarts;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.representation.FlowThroughRep;
import holt.processor.annotation.FlowThroughs;
import holt.processor.activator.Activators;
import holt.processor.activator.DatabaseActivator;
import holt.processor.activator.Activator;
import holt.processor.activator.ExternalEntityActivator;
import holt.processor.activator.ProcessActivator;
import holt.processor.activator.FlowName;
import holt.processor.activator.QueryConnector;

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static holt.processor.DFDParser.loadDfd;

public class DFDsProcessor extends AbstractProcessor {

    public static final String PACKAGE_NAME = "holt.processor.generation";
    public static final String EXTERNAL_ENTITY_PREFIX = "Abstract";
    public static final String PROCESS_PREFIX = "I";
    public static final String DATABASE_PREFIX = "I";

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                DFD.class.getName(),
                FlowStart.class.getName(),
                FlowStarts.class.getName(),
                FlowThrough.class.getName(),
                FlowThroughs.class.getName(),
                Database.class.getName()
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
        Map<DFDName, List<FlowStartRep>> flowStartRepMap = getFlowStarts(convertersResult, environment);
        Map<DFDName, List<FlowThroughRep>> flowThroughRepMap = getFlowThroughs(convertersResult, environment);
        Map<DFDName, List<DatabaseRep>> databaseRepMap = getDatabases(convertersResult, environment);

        for (DFDToJavaFileConverter converter : convertersResult.converters) {
            // Apply annotations to dfd
            if (flowStartRepMap.containsKey(converter.getDFDName())) {
                converter.applyFlowStarts(flowStartRepMap.get(converter.getDFDName()));
            }

            if (flowThroughRepMap.containsKey(converter.getDFDName())) {
                converter.applyFlowThrough(flowThroughRepMap.get(converter.getDFDName()));
            }

            if (databaseRepMap.containsKey(converter.getDFDName())) {
                converter.applyDatabase(databaseRepMap.get(converter.getDFDName()));
            }

            // Convert to java files for each dfd
            saveJavaFiles(converter.convertToJavaFiles());
        }

        return true;
    }

    private record ConvertersResult(List<DFDToJavaFileConverter> converters,
                                    List<Activator> activators,
                                    Map<ActivatorName, DFDName> activatorToDFDMap) { }

    private ConvertersResult getConverters(RoundEnvironment environment) {
        List<DFDToJavaFileConverter> converters = new ArrayList<>();
        List<Activator> allActivators = new ArrayList<>();
        Map<ActivatorName, DFDName> activatorToDFDMap = new HashMap<>();
        Map<DFDName, Map<FlowName, List<Activator>>> flows = new HashMap<>();

        for (Element element : environment.getElementsAnnotatedWith(DFD.class)) {
            DFD dfdAnnotation = element.getAnnotation(DFD.class);
            DFDParser.DFD dfd = loadDfd(toInputStream(dfdAnnotation.csv()), toInputStream(dfdAnnotation.json()));

            DFDName dfdName = new DFDName(dfdAnnotation.name());

            Map<Integer, Activator> idToActivator = new HashMap<>();

            dfd.processes().forEach(node -> idToActivator.put(node.id(), new ProcessActivator(new ActivatorName(node.name()))));
            dfd.databases().forEach(node -> idToActivator.put(node.id(), new DatabaseActivator(new ActivatorName(node.name()))));
            dfd.externalEntities().forEach(node -> idToActivator.put(node.id(), new ExternalEntityActivator(new ActivatorName(node.name()))));

            flows.put(dfdName, new HashMap<>());

            for (Map.Entry<String, List<Dataflow>> entry : dfd.flowsMap().entrySet()) {
                FlowName flowName = new FlowName(entry.getKey());

                flows.get(dfdName).put(flowName, new ArrayList<>());

                // Create Flows
                for (Dataflow dataflow : entry.getValue()) {
                    Activator from = idToActivator.get(dataflow.from().id());

                    // This is used to generate code. Databases are not needed,
                    // since each processor has a reference to all relevant
                    // databases through Connector
                    if (!(from instanceof DatabaseActivator)) {
                        flows.get(dfdName).get(flowName).add(from);
                    }

                    // Add to flows
                    if (from instanceof ProcessActivator fromProcessActivator) {
                        fromProcessActivator.addFlow(flowName);
                    } else if (from instanceof ExternalEntityActivator fromExternalEntityActivator) {
                        fromExternalEntityActivator.addStartFlow(flowName);
                    }
                }

                // Adds the last Activator to flows for the flowName, before it just adds the "to" for each dataflow
                int dataflows = entry.getValue().size();
                Activator lastActivator = idToActivator.get(entry.getValue().get(dataflows - 1).to().id());
                flows.get(dfdName).get(flowName).add(lastActivator);

                // Connect Flows as inputs
                for (Dataflow dataflow : entry.getValue()) {
                    Activator fromActivator = idToActivator.get(dataflow.from().id());
                    Connector connector = null;
                    if (fromActivator instanceof ExternalEntityActivator externalEntityActivator) {
                        connector = externalEntityActivator.getOutput(flowName);
                    } else if (fromActivator instanceof ProcessActivator processActivator) {
                        connector = processActivator.getOutput(flowName);
                    } else if (fromActivator instanceof DatabaseActivator databaseActivator) {
                        connector = new QueryConnector(databaseActivator);
                    }

                    Activator toActivator = idToActivator.get(dataflow.to().id());
                    if (toActivator instanceof ProcessActivator toProcessActivator) {
                        toProcessActivator.addInputToFlow(flowName, connector);
                    } else if (toActivator instanceof ExternalEntityActivator externalEntityActivator) {
                        externalEntityActivator.addEnd(flowName, connector);
                    } else if (toActivator instanceof DatabaseActivator databaseActivator) {
                        databaseActivator.addStore(flowName, connector);
                    }
                }
            }

            List<DatabaseActivator> databaseActivators = new ArrayList<>();
            List<ProcessActivator> processActivators = new ArrayList<>();
            List<ExternalEntityActivator> externalEntities = new ArrayList<>();

            allActivators.addAll(idToActivator.values());
            idToActivator.values().forEach(activator -> activatorToDFDMap.put(activator.name(), dfdName));

            idToActivator.values().forEach(activator -> {
                if (activator instanceof DatabaseActivator databaseActivator) {
                    databaseActivators.add(databaseActivator);
                } else if (activator instanceof ExternalEntityActivator externalEntityActivator) {
                    externalEntities.add(externalEntityActivator);
                } else if (activator instanceof ProcessActivator processActivator) {
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

        return new ConvertersResult(
                converters,
                allActivators,
                activatorToDFDMap
        );
    }

    private Map<DFDName, List<FlowStartRep>> getFlowStarts(ConvertersResult convertersResult,
                                                           RoundEnvironment environment) {
        Map<ActivatorName, DFDName> activatorToDFDMap = convertersResult.activatorToDFDMap;
        List<Activator> activators = convertersResult.activators;

        Map<DFDName, List<FlowStartRep>> dfdToFlowStartRepMap = new HashMap<>();
        for (var flowStartPair : getRepeatableAnnotations(environment, FlowStart.class, FlowStarts.class, FlowStarts::value)) {
            FlowStart flowStart = flowStartPair.annotation;
            TypeElement typeElement = flowStartPair.typeElement;

            ExternalEntityActivator externalEntityActivator = findRelated(ExternalEntityActivator.class, typeElement, activators);
            externalEntityActivator.setQualifiedName(new QualifiedName(typeElement.getQualifiedName().toString()));

            DFDName dfdName = activatorToDFDMap.get(externalEntityActivator.name());
            if (!dfdToFlowStartRepMap.containsKey(dfdName)) {
                dfdToFlowStartRepMap.put(dfdName, new ArrayList<>());
            }
            dfdToFlowStartRepMap
                    .get(dfdName)
                    .add(
                            FlowStartRep
                                    .of(flowStart, externalEntityActivator)
                                    .with(this)
                    );
        }

        return dfdToFlowStartRepMap;
    }

    private Map<DFDName, List<FlowThroughRep>> getFlowThroughs(ConvertersResult convertersResult,
                                                               RoundEnvironment environment) {
        Map<ActivatorName, DFDName> activatorToDFDMap = convertersResult.activatorToDFDMap;
        List<Activator> activators = convertersResult.activators;

        Map<DFDName, List<FlowThroughRep>> dfdToFlowThroughRepMap = new HashMap<>();
        for (var flowThroughPair : getRepeatableAnnotations(environment, FlowThrough.class, FlowThroughs.class, FlowThroughs::value)) {
            FlowThrough flowThrough = flowThroughPair.annotation;
            TypeElement typeElement = flowThroughPair.typeElement;

            ProcessActivator processActivator = findRelated(ProcessActivator.class, typeElement, activators);
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

    private Map<DFDName, List<DatabaseRep>> getDatabases(ConvertersResult convertersResult,
                                                         RoundEnvironment environment) {
        Map<ActivatorName, DFDName> activatorToDFDMap = convertersResult.activatorToDFDMap;
        List<Activator> activators = convertersResult.activators;

        Map<DFDName, List<DatabaseRep>> dfdToDatabasesMap = new HashMap<>();
        for (Element element : environment.getElementsAnnotatedWith(Database.class)) {
            if (element instanceof TypeElement typeElement) {
                DatabaseActivator databaseActivator = findRelated(DatabaseActivator.class, typeElement, activators);
                databaseActivator.setQualifiedName(new QualifiedName(typeElement.getQualifiedName().toString()));
                DFDName dfdName = activatorToDFDMap.get(databaseActivator.name());

                if (!dfdToDatabasesMap.containsKey(dfdName)) {
                    dfdToDatabasesMap.put(dfdName, new ArrayList<>());
                }

                dfdToDatabasesMap.get(dfdName).add(
                        new DatabaseRep(
                                databaseActivator,
                                ClassName.bestGuess(typeElement.toString())
                        )
                );
            }
        }
        return dfdToDatabasesMap;

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

    /*
     * Gets the Activator that is connected to the given typeElement.
     * For example, if a process is the following:
     * @FlowThrough()
     * class FriendProcess implements IFriendProcess {
     *
     * }
     *
     * typeElement would be FriendProcess, and this method would find the Process connected to IFriendProcess.
     *
     * If it finds more than one, then it will throw an IllegalStateException.
     */
    private <T extends Activator> T findRelated(Class<T> entityClass, TypeElement typeElement, List<Activator> activators) {
        Map<String, TypeMirror> typeMirrors = Stream.of(
                Collections.singleton(
                        typeElement.getSuperclass()),
                        typeElement.getInterfaces())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(TypeMirror::toString, Function.identity()));

        List<Activator> hits = activators
                .stream()
                .filter(entityClass::isInstance)
                .filter(entity -> typeMirrors.containsKey(PROCESS_PREFIX + entity.name().value())
                        || typeMirrors.containsKey(EXTERNAL_ENTITY_PREFIX + entity.name().value()))
                .toList();

        if (hits.size() != 1) {
            throw new IllegalStateException("Can only have one hit. Hits found: " + hits);
        }

        return entityClass.cast(hits.get(0));
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
}

