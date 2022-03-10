package holt.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import holt.processor.annotation.DFD;
import holt.processor.annotation.FlowStart;
import holt.processor.annotation.FlowStarts;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.FlowThroughs;
import holt.processor.annotation.Query;
import holt.processor.bond.Bond;
import holt.processor.bond.DatabaseBond;
import holt.processor.bond.ExternalEntityBond;
import holt.processor.bond.ProcessBond;
import holt.processor.bond.BondFlow;
import holt.processor.bond.FlowName;
import holt.processor.bond.QueryBondFlow;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static holt.processor.DFDParser.csvToTable;
import static holt.processor.DFDParser.tableToDfd;

public class DFDProcessor extends AbstractProcessor {

    private final String PACKAGE_NAME = "holt.processor.generation";

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

        Map<DFDName, List<Bond>> dfdMap = loadDFDMap(environment);

        applyFlowStart(dfdMap, environment);
        applyFlowThrough(dfdMap, environment);

        List<JavaFile> javaFiles = toJavaFiles(dfdMap);

        javaFiles.forEach(System.out::println);

        saveJavaFiles(javaFiles);

        return true;
    }

    private Map<DFDName, List<Bond>> loadDFDMap(RoundEnvironment environment) {
        Map<DFDName, List<Bond>> dfdMap = new HashMap<>();

        for (Element element : environment.getElementsAnnotatedWith(DFD.class)) {
            DFD dfdAnnotation = element.getAnnotation(DFD.class);
            DFDParser.DFD dfd = tableToDfd(csvToTable(toInputStream(dfdAnnotation.file())));
            dfdMap.put(new DFDName(dfdAnnotation.name()), toBonds(dfd));
        }

        return dfdMap;
    }

    private void applyFlowStart(Map<DFDName, List<Bond>> dfdMap, RoundEnvironment environment) {
        for (Element element : environment.getElementsAnnotatedWith(FlowStart.class)) {
            if (element instanceof TypeElement typeElement) {
                FlowStart flowStart = typeElement.getAnnotation(FlowStart.class);

                ExternalEntityBond externalEntityBond = (ExternalEntityBond) findRelatedBond(typeElement, dfdMap);
                TypeElement output = asTypeElement(
                        AnnotationValueHelper.getMyValue(typeElement, flowStart,"flowStartType")
                );
                externalEntityBond.setOutputType(new FlowName(flowStart.flow()), output.asType());
            }
        }
    }

    private void applyFlowThrough(Map<DFDName, List<Bond>> dfdMap, RoundEnvironment environment) {
        for (Element element : environment.getElementsAnnotatedWith(FlowThrough.class)) {
            /*
             * Finds the bond by going through the interfaces of the class that @FlowThrough annotates
             */
            if (element instanceof TypeElement typeElement) {
                FlowThrough flowThrough = typeElement.getAnnotation(FlowThrough.class);

                ProcessBond processBond = (ProcessBond) findRelatedBond(typeElement, dfdMap);
                BondFlow bondFlow = processBond.getFlow(new FlowName(flowThrough.flow()));
                bondFlow.setName(flowThrough.functionName());

                TypeElement output = asTypeElement(
                        AnnotationValueHelper.getMyValue(typeElement, flowThrough,"outputType")
                );
                bondFlow.setOutput(output.asType());

                for (Query query : flowThrough.queries()) {
                    String db = query.db();
                    TypeElement type = asTypeElement(
                            AnnotationValueHelper.getAnnotationClassValue(processingEnv.getElementUtils(), query, Query::type)
                    );

                    for (BondFlow inputBondFlow : bondFlow.inputs()) {
                        if (inputBondFlow instanceof QueryBondFlow queryBondFlow) {
                            DatabaseBond databaseBond = queryBondFlow.databaseBond();
                            if (databaseBond.name().equals(db)) {
                                queryBondFlow.setOutput(type.asType());
                            }
                        }
                    }

                }
            }
        }
    }


    /*
     * If it finds more than one, then it will throw an IllegalStateException
     */
    private Bond findRelatedBond(TypeElement typeElement, Map<DFDName, List<Bond>> dfdMap) {
        Map<String, TypeMirror> typeMirrors = Stream.of(
                Collections.singleton(
                        typeElement.getSuperclass()),
                        typeElement.getInterfaces())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(TypeMirror::toString, Function.identity()));

        List<Bond> hits = dfdMap.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(bond -> {
                    if (bond instanceof ProcessBond processBond) {
                        return typeMirrors.containsKey(("I" + processBond.name()));
                    } else if (bond instanceof ExternalEntityBond externalEntityBond) {
                        return typeMirrors.containsKey(("Abstract" + externalEntityBond.name()));
                    } else {
                        return false;
                    }
                })
                .toList();

        if (hits.size() != 1) {
            System.err.println(hits);
            throw new IllegalStateException("Can only have one hit");
        }

        return hits.get(0);
    }

    public void saveJavaFiles(List<JavaFile> javaFiles) {
        javaFiles.forEach(javaFile -> {
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public List<JavaFile> toJavaFiles(Map<DFDName, List<Bond>> dfdMap) {
        List<Bond> bonds = dfdMap.values()
                .stream()
                .flatMap(Collection::stream)
                .toList();

        List<JavaFile> javaFiles = new ArrayList<>();
        Map<DatabaseBond, JavaFile> databaseMap = new HashMap<>();

        dfdMap.forEach((dfdName, dfdBonds) -> {
            String enumName = dfdName.value().substring(0, 1).toUpperCase() + dfdName.value().substring(1);
            TypeSpec.Builder dfdFlowEnumBuilder = TypeSpec.enumBuilder(enumName);

            dfdBonds
                    .stream()
                    .filter(bond -> bond instanceof ExternalEntityBond)
                    .map(bond -> (ExternalEntityBond) bond)
                    .<String>mapMulti((externalEntityBond, consumer) -> {
                        externalEntityBond.starts().keySet().forEach(flowName -> {
                            consumer.accept(flowName.value().toUpperCase());
                        });
                    })
                    .distinct()
                    .forEach(dfdFlowEnumBuilder::addEnumConstant);

            javaFiles.add(JavaFile.builder(PACKAGE_NAME, dfdFlowEnumBuilder.build()).build());
        });

        bonds
                .stream()
                .filter(bond -> bond instanceof DatabaseBond)
                .map(bond -> (DatabaseBond) bond)
                .map(databaseBond -> {
                    TypeSpec databaseSpec = TypeSpec.interfaceBuilder("I" + databaseBond.name())
                            .addModifiers(Modifier.PUBLIC)
                            .build();

                    JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, databaseSpec).build();
                    databaseMap.put(databaseBond, javaFile);
                    return javaFile;
                })
                .forEach(javaFiles::add);

        bonds.stream()
                .filter(bond -> (bond instanceof ExternalEntityBond))
                .map(bond -> {
                    ExternalEntityBond externalEntityBond = (ExternalEntityBond) bond;

                    TypeSpec.Builder externalEntityBuilder = TypeSpec
                            .classBuilder("Abstract" + externalEntityBond.name())
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

                    CodeBlock comment = CodeBlock.builder().add("// TODO: Call Holt?").build();

                    externalEntityBond.starts().forEach((flowName, bondFlow) -> {
                        MethodSpec methodSpec = MethodSpec
                                .methodBuilder(flowName.value())
                                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                .addCode(comment)
                                .build();

                        externalEntityBuilder.addMethod(methodSpec);
                    });

                    return JavaFile.builder(PACKAGE_NAME, externalEntityBuilder.build()).build();
                })
                .forEach(javaFiles::add);

        bonds
                .stream()
                .filter(bond -> bond instanceof ProcessBond)
                .map(bond -> {
                    ProcessBond processBond = (ProcessBond) bond;

                    List<JavaFile> newFiles = new ArrayList<>();

                    TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder("I" + processBond.name())
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

                    for (BondFlow bondFlow : processBond.methods()) {
                        MethodSpec.Builder methodSpecBuilder = MethodSpec
                                .methodBuilder(bondFlow.name())
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

                        int i = 0;
                        for (BondFlow bondFlowInput : bondFlow.inputs()) {
                            ClassName parameterClassName;
                            if (bondFlowInput.output() != null) {
                                parameterClassName = ClassName.bestGuess(bondFlowInput.output().toString());
                            } else {
                                parameterClassName = ClassName.get(Object.class);
                            }
                            String parameterName = "input" + i;
                            if (bondFlowInput instanceof QueryBondFlow) {
                                parameterName = "dbInput" + i;
                            }
                            methodSpecBuilder.addParameter(parameterClassName, parameterName);
                            i++;
                        }

                        // Databases
                        for (BondFlow bondFlowInput : bondFlow.inputs()) {
                            if (bondFlowInput instanceof QueryBondFlow queryBondFlow) {
                                // First add query interface
                                String databaseName = databaseMap.get(queryBondFlow.databaseBond()).typeSpec.name;
                                ClassName databaseClassname = ClassName.bestGuess(PACKAGE_NAME + "." + databaseName);
                                TypeSpec queryInterfaceSpec = createQueryInterface(queryBondFlow, databaseName + "To" + processBond.name() + bondFlow.name() + "Query", databaseClassname);
                                newFiles.add(JavaFile.builder(PACKAGE_NAME, queryInterfaceSpec).build());

                                // Then add method to create that interface
                                ClassName returnClass = ClassName.bestGuess(PACKAGE_NAME + "." + queryInterfaceSpec.name);
                                MethodSpec.Builder queryMethodSpecBuilder = MethodSpec
                                        .methodBuilder("query_" + queryBondFlow.databaseBond().name() + "_" + bondFlow.name())
                                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                        .returns(returnClass);

                                bondFlow.inputs()
                                        .stream()
                                        .filter(b -> !(b instanceof QueryBondFlow))
                                        .forEach(dbInput -> {
                                            ClassName parameterClassName;
                                            if (dbInput.output() != null) {
                                                parameterClassName = ClassName.bestGuess(dbInput.output().toString());
                                            } else {
                                                parameterClassName = ClassName.get(Object.class);
                                            }
                                            queryMethodSpecBuilder.addParameter(
                                                    parameterClassName,
                                                    "input",
                                                    Modifier.FINAL
                                            );
                                        });

                                interfaceBuilder.addMethod(queryMethodSpecBuilder.build());
                            }
                        }

                        ClassName returnClassName = ClassName.get(Object.class);
                        methodSpecBuilder.returns(returnClassName);

                        interfaceBuilder.addMethod(methodSpecBuilder.build());
                    }

                    newFiles.add(JavaFile.builder(PACKAGE_NAME, interfaceBuilder.build()).build());

                    return newFiles;
                })
                .forEach(javaFiles::addAll);

        return javaFiles;
    }

    private TypeSpec createQueryInterface(QueryBondFlow queryBondFlow, String queryInterfaceName, ClassName databaseClassname) {
        ClassName returnQueryType;
        if (queryBondFlow.output() != null) {
            returnQueryType = ClassName.bestGuess(queryBondFlow.output().toString());
        } else {
            returnQueryType = ClassName.get(Object.class);
        }

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

    public List<Bond> toBonds(DFDParser.DFD dfd) {
        final Map<Integer, Bond> bonds = new HashMap<>();

        dfd.processes().forEach(node -> bonds.put(node.id(), new ProcessBond(node.name())));
        dfd.databases().forEach(node -> bonds.put(node.id(), new DatabaseBond(node.name())));
        dfd.externalEntities().forEach(node -> bonds.put(node.id(), new ExternalEntityBond(node.name())));

        for (Map.Entry<String, List<Dataflow>> entry : dfd.flowsMap().entrySet()) {
            FlowName flowName = new FlowName(entry.getKey());

            // Create BondFlows
            for (Dataflow dataflow : entry.getValue()) {
                Bond to = bonds.get(dataflow.to().id());
                if (to instanceof ProcessBond processTo) {
                    processTo.addMethod(flowName, new BondFlow());
                }
            }

            // Connect BondFlows as inputs
            for (Dataflow dataflow : entry.getValue()) {
                Bond fromBond = bonds.get(dataflow.from().id());
                BondFlow bondFlow = null;
                if (fromBond instanceof ExternalEntityBond externalEntityBond) {
                    bondFlow = externalEntityBond.addFlow(flowName);
                } else if (fromBond instanceof ProcessBond processBond) {
                    bondFlow = processBond.getFlow(flowName);
                } else if (fromBond instanceof DatabaseBond databaseBond) {
                    bondFlow = new QueryBondFlow(databaseBond);
                }

                Bond toBond = bonds.get(dataflow.to().id());
                if (toBond instanceof ProcessBond toProcessBond) {
                    toProcessBond.getFlow(flowName).addInput(bondFlow);
                }
            }
        }

        return bonds.values().stream().toList();
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

    private TypeElement asTypeElement(TypeMirror typeMirror) {
        Types TypeUtils = this.processingEnv.getTypeUtils();
        return (TypeElement) TypeUtils.asElement(typeMirror);
    }

}

