package holt.processor;

import com.squareup.javapoet.JavaFile;
import holt.processor.activator.ActivatorName;
import holt.processor.activator.Connector;
import holt.processor.annotation.DFD;
import holt.processor.annotation.FlowStart;
import holt.processor.annotation.FlowStartRep;
import holt.processor.annotation.FlowStarts;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.FlowThroughRep;
import holt.processor.annotation.FlowThroughs;
import holt.processor.activator.Activators;
import holt.processor.activator.Database;
import holt.processor.activator.Activator;
import holt.processor.activator.ExternalEntity;
import holt.processor.activator.Process;
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
                FlowThroughs.class.getName()
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

        for (DFDToJavaFileConverter converter : convertersResult.converters) {
            // Apply annotations to dfd
            converter.applyFlowStarts(flowStartRepMap.get(converter.getDFDName()));
            converter.applyFlowThrough(flowThroughRepMap.get(converter.getDFDName()));

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

        for (Element element : environment.getElementsAnnotatedWith(DFD.class)) {
            DFD dfdAnnotation = element.getAnnotation(DFD.class);
            DFDParser.DFD dfd = loadDfd(toInputStream(dfdAnnotation.file()));

            DFDName dfdName = new DFDName(dfdAnnotation.name());

            Map<Integer, Activator> idToActivator = new HashMap<>();

            dfd.processes().forEach(node -> idToActivator.put(node.id(), new Process(new ActivatorName(node.name()), dfdName)));
            dfd.databases().forEach(node -> idToActivator.put(node.id(), new Database(new ActivatorName(node.name()), dfdName)));
            dfd.externalEntities().forEach(node -> idToActivator.put(node.id(), new ExternalEntity(new ActivatorName(node.name()), dfdName)));

            for (Map.Entry<String, List<Dataflow>> entry : dfd.flowsMap().entrySet()) {
                FlowName flowName = new FlowName(entry.getKey());
                System.out.println(entry.getKey());
                // Create Flows
                for (Dataflow dataflow : entry.getValue()) {
                    Activator from = idToActivator.get(dataflow.from().id());
                    if (from instanceof Process fromProcess) {
                        fromProcess.addFlow(flowName);
                    } else if (from instanceof ExternalEntity fromExternalEntity) {
                        fromExternalEntity.addStartFlow(flowName);
                    }
                }


                idToActivator.values().forEach(System.out::println);

                // Connect Flows as inputs
                for (Dataflow dataflow : entry.getValue()) {
                    Activator fromActivator = idToActivator.get(dataflow.from().id());
                    Connector connector = null;
                    if (fromActivator instanceof ExternalEntity externalEntity) {
                        connector = externalEntity.getOutput(flowName);
                    } else if (fromActivator instanceof Process process) {
                        connector = process.getOutput(flowName);
                    } else if (fromActivator instanceof Database database) {
                        connector = new QueryConnector(database);
                    }

                    Activator toActivator = idToActivator.get(dataflow.to().id());
                    if (toActivator instanceof Process toProcess) {
                        toProcess.addInputToFlow(flowName, connector);
                    } else if (toActivator instanceof ExternalEntity externalEntity) {
                        externalEntity.addEnd(flowName, connector);
                    }
                }
            }

            idToActivator.values().forEach(System.out::println);

            List<Database> databases = new ArrayList<>();
            List<Process> processes = new ArrayList<>();
            List<ExternalEntity> externalEntities = new ArrayList<>();

            allActivators.addAll(idToActivator.values());
            idToActivator.values().forEach(activator -> activatorToDFDMap.put(activator.name(), dfdName));

            idToActivator.values().forEach(activator -> {
                if (activator instanceof Database database) {
                    databases.add(database);
                } else if (activator instanceof ExternalEntity externalEntity) {
                    externalEntities.add(externalEntity);
                } else if (activator instanceof Process process) {
                    processes.add(process);
                }
            });

            converters.add(new DFDToJavaFileConverter(
                    dfdName,
                    new Activators(
                            databases,
                            externalEntities,
                            processes
                    )
            ));
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

            ExternalEntity externalEntity = findRelated(ExternalEntity.class, typeElement, activators);
            DFDName dfdName = activatorToDFDMap.get(externalEntity.name());
            if (!dfdToFlowStartRepMap.containsKey(dfdName)) {
                dfdToFlowStartRepMap.put(dfdName, new ArrayList<>());
            }
            dfdToFlowStartRepMap
                    .get(dfdName)
                    .add(
                            FlowStartRep
                                    .of(flowStart, externalEntity)
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

            Process process = findRelated(Process.class, typeElement, activators);
            DFDName dfdName = activatorToDFDMap.get(process.name());

            if (!dfdToFlowThroughRepMap.containsKey(dfdName)) {
                dfdToFlowThroughRepMap.put(dfdName, new ArrayList<>());
            }

            dfdToFlowThroughRepMap
                    .get(dfdName)
                    .add(FlowThroughRep
                            .of(process, flowThrough)
                            .with(this)
                    );

        }

        return dfdToFlowThroughRepMap;
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
     * If it finds more than one, then it will throw an IllegalStateException
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
            System.err.println(hits);
            throw new IllegalStateException("Can only have one hit");
        }

        return entityClass.cast(hits.get(0));
    }

    private void saveJavaFiles(List<JavaFile> javaFiles) {
        javaFiles.forEach(javaFile -> {
            try {
                System.out.println(javaFile);
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

