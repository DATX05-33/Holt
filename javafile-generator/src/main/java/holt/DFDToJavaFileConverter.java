package holt;

import com.squareup.javapoet.*;
import holt.representation.FlowThroughRep;
import holt.representation.QueriesForRep;
import holt.representation.QueryDefinitionRep;
import holt.representation.TraverseRep;

import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DFDToJavaFileConverter {

    private static final String PACKAGE_NAME = "holt.processor.generation";

    private final DFDName dfdName;
    private final Domain domain;
    private final String dfdPackageName;

    public DFDToJavaFileConverter(DFDName dfdName, Domain domain) {
        this.dfdName = dfdName;
        this.domain = domain;
        this.dfdPackageName = PACKAGE_NAME + "." + dfdName;
    }

    public void applyTraverses(List<TraverseRep> traverseReps) {
        for (TraverseRep traverseRep : traverseReps) {
            traverseRep.externalEntityActivator().setOutputType(traverseRep.name(), traverseRep.flowStartType());
        }
    }

    public void applyFlowThrough(List<FlowThroughRep> flowThroughReps) {
        for (FlowThroughRep flowThroughRep : flowThroughReps) {
            TraverseName traverseName = flowThroughRep.traverseName();
            FlowThroughAggregate flowThrough = flowThroughRep.processActivator().flow(traverseName);
            flowThrough.setOutputType(flowThroughRep.outputType());
            flowThrough.setFunctionName(new FunctionName(flowThroughRep.functionName()));

            flowThroughRep.queries().forEach(query -> {
                for (QueryInput queryInput : flowThrough.queries()) {
                    DatabaseActivatorAggregate databaseActivator = queryInput.queryInputDefinition().database();
                    if (databaseActivator.name().value().equals(query.db().simpleName())) {
                        queryInput.queryInputDefinition().setOutput(query.type());
                    }
                }
            });

            for (QueryDefinitionRep queryDefinitionRep : flowThroughRep.overrideQueries()) {
                queryDefinitionRep.process().flow(traverseName).moveQueryInputDefinitionTo(
                        queryDefinitionRep.db(),
                        flowThrough
                );
            }
        }
    }

    public void applyQueriesFor(List<QueriesForRep> queriesForReps) {
        for (QueriesForRep queriesForRep : queriesForReps) {
            // There can only be one queries for per database
            if (queriesForRep.databaseActivatorAggregate().getQueriesClassName() != null) {
                throw new IllegalStateException("There can only be one @QueriesFor per database");
            }

            queriesForRep.databaseActivatorAggregate().setQueriesClassName(queriesForRep.queriesClassName());
        }
    }

    private List<JavaFile> convertToJavaFiles() {
        List<JavaFile> javaFiles = new ArrayList<>();
        Map<DatabaseActivatorAggregate, JavaFile> databaseMap = new HashMap<>();

        domain
                .databases()
                .forEach(database -> {
                    JavaFile databaseJavaFile = this.generateDatabaseJavaFile(database);

                    javaFiles.add(databaseJavaFile);
                    databaseMap.put(database, databaseJavaFile);
                });

        domain
                .externalEntities()
                .forEach(externalEntity -> javaFiles.add(this.generateExternalEntityJavaFile(domain, externalEntity)));

        domain
                .processes()
                .forEach(process -> javaFiles.addAll(
                        this.generateProcessJavaFile(process, databaseMap)
                ));

        return javaFiles;
    }

    public DFDName getDFDName() {
        return this.dfdName;
    }

    private List<JavaFile> generateProcessJavaFile(ProcessActivatorAggregate processActivator, Map<DatabaseActivatorAggregate, JavaFile> databaseMap) {
        List<JavaFile> newFiles = new ArrayList<>();

        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(processActivator.requirementsName().value())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(getGeneratedAnnotation());

        for (FlowThroughAggregate flowThrough : processActivator.flows()) {
            MethodSpec.Builder methodSpecBuilder = MethodSpec
                    .methodBuilder(flowThrough.functionName().value())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

            int i = 0;
            for (Connector connector : flowThrough.inputs()) {
                ClassName parameterClassName = toClassName(connector.type());
                methodSpecBuilder.addParameter(
                        parameterClassName,
                        "input" + i
                );
                i++;
            }

            for (QueryInput queryInput : flowThrough.queries()) {
                ClassName parameterClassName = toClassName(queryInput.queryInputDefinition().output());
                methodSpecBuilder.addParameter(parameterClassName, "dbInput" + i);
                i++;
            }

            ClassName returnClassName = toClassName(flowThrough.output().type());
            methodSpecBuilder.returns(returnClassName);

            interfaceBuilder.addMethod(methodSpecBuilder.build());

            // Databases queries definitions
            for (QueryInputDefinition queryInputDefinition : flowThrough.queryInputDefinitions()) {
                // Database that is going to be queried from
                DatabaseActivatorAggregate database = queryInputDefinition.database();

                // First add query interface
                String databaseRequirementsName = databaseMap.get(queryInputDefinition.database()).typeSpec.name;

                // Find what the type of db should be used. Either querier, db or db requirements
                ClassName databaseClassname;
                if (database.getQueriesClassName() != null) {
                    databaseClassname = toClassName(database.getQueriesClassName());
                } else if (database.connectedClass().isPresent()) {
                    databaseClassname = toClassName(database.connectedClass().get().qualifiedName());
                } else {
                    databaseClassname = toClassName(dfdPackageName + "." + databaseRequirementsName);
                }

                String databaseName = database.name().value();
                String queryInterfaceName = processActivator.getQueryInterfaceNameForDatabase(database, flowThrough);

                TypeSpec queryInterfaceSpec = generateQuery(queryInputDefinition, queryInterfaceName, databaseClassname);
                newFiles.add(JavaFile.builder(dfdPackageName, queryInterfaceSpec).build());

                ClassName returnClass = ClassName.bestGuess(dfdPackageName + "." + queryInterfaceSpec.name);
                String queryMethodName = processActivator.getQueryMethodNameForDatabase(database, flowThrough);
                MethodSpec.Builder queryMethodSpecBuilder = MethodSpec
                        .methodBuilder(queryMethodName)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(returnClass);

                List<Connector> inputsForQueryDefinition = flowThrough.inputs();
                for (int j = 0; j < inputsForQueryDefinition.size(); j++) {
                    Connector connector = inputsForQueryDefinition.get(j);
                    queryMethodSpecBuilder.addParameter(
                            toClassName(connector.type()),
                            "input" + j
                    );
                }

                interfaceBuilder.addMethod(queryMethodSpecBuilder.build());
            }
        }

        newFiles.add(JavaFile.builder(dfdPackageName, interfaceBuilder.build()).build());

        return newFiles;
    }

    private JavaFile generateExternalEntityJavaFile(Domain domain, ExternalEntityActivatorAggregate externalEntityActivator) {
        return TraversesGenerator.generateExternalEntityJavaFile(domain, dfdPackageName, externalEntityActivator);
    }

    private JavaFile generateDatabaseJavaFile(DatabaseActivatorAggregate databaseActivator) {
        TypeSpec.Builder databaseSpec = TypeSpec.interfaceBuilder(databaseActivator.requirementsName().value())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(getGeneratedAnnotation());

        List<MethodSpec> stores = toOutputMethods(new ArrayList<>(databaseActivator.outputs().values()));
        databaseSpec.addMethods(stores);

        if (databaseActivator.getQueriesClassName() != null) {
            MethodSpec getQueriesInstance = MethodSpec.methodBuilder("getQuerierInstance")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(toClassName(databaseActivator.getQueriesClassName()))
                    .build();

            databaseSpec.addMethod(getQueriesInstance);
        }

        return JavaFile.builder(dfdPackageName, databaseSpec.build()).build();
    }

    private TypeSpec generateQuery(QueryInputDefinition queryInputDefinition, String queryInterfaceName, ClassName databaseClassname) {
        ClassName returnQueryType = toClassName(queryInputDefinition.output());

        MethodSpec queryMethod = MethodSpec
                .methodBuilder("createQuery")
                .addParameter(databaseClassname, "db")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(returnQueryType)
                .build();

        return TypeSpec
                .interfaceBuilder(queryInterfaceName)
                .addAnnotation(getGeneratedAnnotation())
                .addMethod(queryMethod)
                .addModifiers(Modifier.PUBLIC)
                .build();
    }

    public static List<MethodSpec> toOutputMethods(List<TraverseOutput> outputs) {
        List<MethodSpec> outputMethods = new ArrayList<>();
        outputs.forEach(traverseOutput -> {
            var methodSpecBuilder = MethodSpec.methodBuilder(traverseOutput.functionName().value())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

            for (int i = 0; i < traverseOutput.inputs().size(); i++) {
                Connector connector = traverseOutput.inputs().get(i);
                methodSpecBuilder.addParameter(toClassName(connector.type()), "input" + i);
            }

            outputMethods.add(methodSpecBuilder.build());
        });

        return outputMethods;
    }

    public void saveJavaFiles(ProcessingEnvironment processingEnv) {
        List<JavaFile> javaFiles = convertToJavaFiles();
        for (JavaFile javaFile : javaFiles) {
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static AnnotationSpec getGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("\"holt.processor.DFDsProcessor\""))
                .build();
    }

    public static ClassName toClassName(QualifiedName qualifiedName) {
        return ClassName.bestGuess(qualifiedName.value());
    }

    public static ClassName toClassName(String qualifiedName) {
        return ClassName.bestGuess(qualifiedName);
    }
}
