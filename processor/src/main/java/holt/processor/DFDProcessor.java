package holt.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import holt.processor.annotation.DFD;
import holt.processor.annotation.FlowStart;
import holt.processor.annotation.FlowStarts;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.FlowThroughs;
import holt.processor.annotation.Query;
import holt.processor.activator.Activators;
import holt.processor.activator.Database;
import holt.processor.activator.Activator;
import holt.processor.activator.ExternalEntity;
import holt.processor.activator.Process;
import holt.processor.activator.Flow;
import holt.processor.activator.FlowName;
import holt.processor.activator.QueryFlow;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
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

import static holt.processor.DFDParser.csvToTable;
import static holt.processor.DFDParser.tableToDfd;

public class DFDProcessor extends AbstractProcessor {

    private static final String PACKAGE_NAME = "holt.processor.generation";
    private static final String EXTERNAL_ENTITY_PREFIX = "Abstract";
    private static final String PROCESS_PREFIX = "I";
    private static final String DATABASE_PREFIX = "I";

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

        Map<DFDName, Activators> dfdMap = loadDFDMap(environment);

        applyFlowStart(dfdMap, environment);
        applyFlowThrough(dfdMap, environment);

        List<JavaFile> javaFiles = toJavaFiles(dfdMap);

        saveJavaFiles(javaFiles);

        return true;
    }

    private Map<DFDName, Activators> loadDFDMap(RoundEnvironment environment) {
        Map<DFDName, Activators> dfdMap = new HashMap<>();

        for (Element element : environment.getElementsAnnotatedWith(DFD.class)) {
            DFD dfdAnnotation = element.getAnnotation(DFD.class);
            DFDParser.DFD dfd = tableToDfd(csvToTable(toInputStream(dfdAnnotation.file())));
            dfdMap.put(new DFDName(dfdAnnotation.name()), toActivators(dfd));
        }

        return dfdMap;
    }

    private void applyFlowStart(Map<DFDName, Activators> dfdMap, RoundEnvironment environment) {
        for (var flowStartPair : getRepeatableAnnotations(environment, FlowStart.class, FlowStarts.class, FlowStarts::value)) {
            FlowStart flowStart = flowStartPair.annotation;
            TypeElement typeElement = flowStartPair.typeElement;

            ExternalEntity externalEntityBond = findRelated(ExternalEntity.class, typeElement, dfdMap);
            TypeElement output = getAnnotationClassValue(flowStart, FlowStart::flowStartType);

            externalEntityBond.setOutputType(new FlowName(flowStart.flow()), output.asType());
        }
    }

    private void applyFlowThrough(Map<DFDName, Activators> dfdMap, RoundEnvironment environment) {
        /*
         * Finds the activator by going through the interfaces of the class that @FlowThrough annotates
         */
        for (var flowThroughPair : getRepeatableAnnotations(environment, FlowThrough.class, FlowThroughs.class, FlowThroughs::value)){
            FlowThrough flowThrough = flowThroughPair.annotation;
            TypeElement typeElement = flowThroughPair.typeElement;

            Process processBond = findRelated(Process.class, typeElement, dfdMap);
            Flow flow = processBond.getFlow(new FlowName(flowThrough.flow()));
            flow.setName(flowThrough.functionName());

            TypeElement output = getAnnotationClassValue(flowThrough, FlowThrough::outputType);
            flow.setOutput(output.asType());

            for (Query query : flowThrough.queries()) {
                TypeElement dbType = getAnnotationClassValue(query, Query::db);
                TypeElement type = getAnnotationClassValue(query, Query::type);

                for (Flow inputBondFlow : flow.inputs()) {
                    if (inputBondFlow instanceof QueryFlow queryBondFlow) {
                        Database databaseBond = queryBondFlow.database();
                        if ((databaseBond.name()).equals(dbType.getSimpleName().toString())) {
                            queryBondFlow.setOutput(type.asType());
                        }
                    }
                }
            }
        }

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
    private <T extends Activator> T findRelated(Class<T> entityClass, TypeElement typeElement, Map<DFDName, Activators> dfdMap) {
        Map<String, TypeMirror> typeMirrors = Stream.of(
                Collections.singleton(
                        typeElement.getSuperclass()),
                        typeElement.getInterfaces())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(TypeMirror::toString, Function.identity()));

        List<Activator> hits = dfdMap.values()
                .stream()
                .flatMap(Activators::nodeStream)
                .filter(entityClass::isInstance)
                .filter(entity -> typeMirrors.containsKey(PROCESS_PREFIX + entity.name())
                        || typeMirrors.containsKey(EXTERNAL_ENTITY_PREFIX + entity.name()))
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
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private List<JavaFile> toJavaFiles(Map<DFDName, Activators> dfdMap) {
        List<JavaFile> javaFiles = new ArrayList<>();
        Map<Database, JavaFile> databaseMap = new HashMap<>();

        Collection<Activators> activators = dfdMap.values();

        activators
                .stream()
                .map(Activators::databaseBonds)
                .flatMap(Collection::stream)
                .forEach(database -> {
                    JavaFile databaseJavaFile = this.generateDatabaseJavaFile(database);

                    javaFiles.add(databaseJavaFile);
                    databaseMap.put(database, databaseJavaFile);
                });

        activators
                .stream()
                .map(Activators::externalEntityBonds)
                .flatMap(Collection::stream)
                .map(this::generateExternalEntityJavaFile)
                .forEach(javaFiles::add);

        activators
                .stream()
                .map(Activators::processBonds)
                .flatMap(Collection::stream)
                .map(process -> this.generateProcessJavaFile(process, databaseMap))
                .forEach(javaFiles::addAll);

        return javaFiles;
    }

    private List<JavaFile> generateProcessJavaFile(Process process, Map<Database, JavaFile> databaseMap) {
        List<JavaFile> newFiles = new ArrayList<>();

        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(PROCESS_PREFIX + process.name())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        for (Flow flow : process.methods()) {
            MethodSpec.Builder methodSpecBuilder = MethodSpec
                    .methodBuilder(flow.name())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

            int i = 0;
            for (Flow inputFlow : flow.inputs()) {
                ClassName parameterClassName = inputFlow.output();

                String parameterName = "input" + i;
                if (inputFlow instanceof QueryFlow) {
                    parameterName = "dbInput" + i;
                }

                methodSpecBuilder.addParameter(parameterClassName, parameterName);
                i++;
            }

            // Databases
            for (Flow flowInput : flow.inputs()) {
                if (flowInput instanceof QueryFlow queryFlow) {
                    // First add query interface
                    String databaseName = databaseMap.get(queryFlow.database()).typeSpec.name;
                    ClassName databaseClassname = ClassName.bestGuess(PACKAGE_NAME + "." + databaseName);
                    TypeSpec queryInterfaceSpec = generateQuery(queryFlow, databaseName + "To" + ((Process) process).name() + flow.name() + "Query", databaseClassname);
                    newFiles.add(JavaFile.builder(PACKAGE_NAME, queryInterfaceSpec).build());

                    // Then add method to create that interface
                    ClassName returnClass = ClassName.bestGuess(PACKAGE_NAME + "." + queryInterfaceSpec.name);
                    MethodSpec.Builder queryMethodSpecBuilder = MethodSpec
                            .methodBuilder("query_" + queryFlow.database().name() + "_" + flow.name())
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .returns(returnClass);

                    int j = 0;
                    for (Flow dbInput : flow.inputs()) {
                        if (dbInput instanceof QueryFlow) {
                            ClassName parameterClassName = dbInput.output();
                            queryMethodSpecBuilder.addParameter(
                                    parameterClassName,
                                    "input" + j,
                                    Modifier.FINAL
                            );
                            j++;
                        }
                    }

                    interfaceBuilder.addMethod(queryMethodSpecBuilder.build());
                }
            }

            ClassName returnClassName = flow.output();
            methodSpecBuilder.returns(returnClassName);

            interfaceBuilder.addMethod(methodSpecBuilder.build());
        }

        newFiles.add(JavaFile.builder(PACKAGE_NAME, interfaceBuilder.build()).build());

        return newFiles;
    }

    private JavaFile generateExternalEntityJavaFile(ExternalEntity externalEntity) {
        TypeSpec.Builder externalEntityBuilder = TypeSpec
                .classBuilder(EXTERNAL_ENTITY_PREFIX + externalEntity.name())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        externalEntity
                .starts()
                .entrySet()
                .stream()
                .map(flowNameFlowEntry ->
                        this.generateExternalEntityStartMethod(
                                externalEntity,
                                flowNameFlowEntry.getKey(),
                                flowNameFlowEntry.getValue()
                        )
                )
                .forEach(externalEntityBuilder::addMethod);

        return JavaFile.builder(PACKAGE_NAME, externalEntityBuilder.build()).build();
    }

    private MethodSpec generateExternalEntityStartMethod(ExternalEntity externalEntity, FlowName flowName, Flow startFlow) {
        ClassName parameterClassType = startFlow.output();
        ParameterSpec dataParameterSpec = ParameterSpec.builder(parameterClassType, "d").build();
        ParameterSpec policyParameterSpec = ParameterSpec.builder(Object.class, "pol").build();

        CodeBlock comment = CodeBlock.builder().add("// TODO: Call Holt?\n").build();

        MethodSpec.Builder methodSpecBuilder = MethodSpec
                .methodBuilder(flowName.value())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addCode(comment)
                .addParameter(dataParameterSpec)
                .addParameter(policyParameterSpec);

        externalEntity.end(flowName)
                .ifPresent(flow -> {
                    CodeBlock returnStatement = CodeBlock.builder().add("return null;").build();
                    methodSpecBuilder.addCode(returnStatement);
                    ClassName returnClassType = flow.output();
                    methodSpecBuilder.returns(returnClassType);
                });

        return methodSpecBuilder.build();
    }

    private JavaFile generateDatabaseJavaFile(Database database) {
        TypeSpec databaseSpec = TypeSpec.interfaceBuilder(DATABASE_PREFIX + database.name())
                .addModifiers(Modifier.PUBLIC)
                .build();

        return JavaFile.builder(PACKAGE_NAME, databaseSpec).build();
    }

    private TypeSpec generateQuery(QueryFlow queryBondFlow, String queryInterfaceName, ClassName databaseClassname) {
        ClassName returnQueryType = queryBondFlow.output();

        MethodSpec queryMethod = MethodSpec
                .methodBuilder("createQuery")
                .addParameter(databaseClassname, "db")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(returnQueryType)
                .build();

        return TypeSpec
                .interfaceBuilder(queryInterfaceName)
                .addMethod(queryMethod)
                .addModifiers(Modifier.PUBLIC)
                .build();
    }

    private Activators toActivators(DFDParser.DFD dfd) {
        final Map<Integer, Activator> idToActivator = new HashMap<>();

        dfd.processes().forEach(node -> idToActivator.put(node.id(), new Process(node.name())));
        dfd.databases().forEach(node -> idToActivator.put(node.id(), new Database(node.name())));
        dfd.externalEntities().forEach(node -> idToActivator.put(node.id(), new ExternalEntity(node.name())));

        for (Map.Entry<String, List<Dataflow>> entry : dfd.flowsMap().entrySet()) {
            FlowName flowName = new FlowName(entry.getKey());

            // Create Flows
            for (Dataflow dataflow : entry.getValue()) {
                Activator to = idToActivator.get(dataflow.to().id());
                if (to instanceof Process processTo) {
                    processTo.addMethod(flowName, new Flow());
                }
            }

            // Connect Flows as inputs
            for (Dataflow dataflow : entry.getValue()) {
                Activator fromActivator = idToActivator.get(dataflow.from().id());
                Flow flow = null;
                if (fromActivator instanceof ExternalEntity externalEntityBond) {
                    flow = externalEntityBond.addFlow(flowName);
                } else if (fromActivator instanceof Process processBond) {
                    flow = processBond.getFlow(flowName);
                } else if (fromActivator instanceof Database databaseBond) {
                    flow = new QueryFlow(databaseBond);
                }

                Activator toActivator = idToActivator.get(dataflow.to().id());
                if (toActivator instanceof Process toProcessBond) {
                    toProcessBond.getFlow(flowName).addInput(flow);
                } else if (toActivator instanceof ExternalEntity externalEntityBond) {
                    externalEntityBond.addEnd(flowName, flow);
                }
            }
        }

        List<Database> databases = new ArrayList<>();
        List<Process> processes = new ArrayList<>();
        List<ExternalEntity> externalEntities = new ArrayList<>();

        idToActivator.values().forEach(activator -> {
            if (activator instanceof Database database) {
                databases.add(database);
            } else if (activator instanceof ExternalEntity externalEntity) {
                externalEntities.add(externalEntity);
            } else if (activator instanceof Process process) {
                processes.add(process);
            }
        });

        return new Activators(
                databases,
                externalEntities,
                processes
        );
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

    private <T extends Annotation> TypeElement getAnnotationClassValue(T anno, Function<T, Class<?>> func) {
        Elements elements = this.processingEnv.getElementUtils();

        // TODO: Find a better way rather than try/catch
        TypeMirror typeMirror;
        try {
            typeMirror = elements.getTypeElement(func.apply(anno).getCanonicalName()).asType();
        } catch (MirroredTypeException e) {
            typeMirror = e.getTypeMirror();
        }

        return (TypeElement) this.processingEnv.getTypeUtils().asElement(typeMirror);
    }


}

