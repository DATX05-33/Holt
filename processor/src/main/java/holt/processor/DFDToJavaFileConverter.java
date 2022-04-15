package holt.processor;

import com.squareup.javapoet.*;
import holt.processor.activator.*;
import holt.processor.annotation.representation.FlowThroughRep;
import holt.processor.annotation.representation.QueriesForRep;
import holt.processor.annotation.representation.QueryDefinitionRep;
import holt.processor.annotation.representation.TraverseRep;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static holt.processor.DFDsProcessor.*;

public class DFDToJavaFileConverter {

    private final DFDName dfdName;
    private final Activators activators;
    private final String dfdPackageName;

    public DFDToJavaFileConverter(DFDName dfdName, Activators activators) {
        this.dfdName = dfdName;
        this.activators = activators;
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

    /**
     *  for (Connector input : queryDefinitionRep.process().queryConnectorsDefinitions()) {
     *                     System.out.println("Me, " + queryDefinitionRep.process().name() + "; have input:  " + input);
     *                     if (input instanceof QueryConnector queryConnector) {
     *                         System.out.println(queryDefinitionRep.db().simpleName() + " ?= " + queryConnector.database().name().value());
     *                         if (queryDefinitionRep.db().simpleName().equals(queryConnector.database().name().value())) {
                  }
     *                     }
     *                 }
     * @param queriesForReps
     */

    public void applyQueriesFor(List<QueriesForRep> queriesForReps) {
        for (QueriesForRep queriesForRep : queriesForReps) {
            // There can only be one queries for per database
            if (queriesForRep.databaseActivatorAggregate().getQueriesClassName() != null) {
                throw new IllegalStateException("There can only be one @QueriesFor per database");
            }

            queriesForRep.databaseActivatorAggregate().setQueriesClassName(queriesForRep.queriesClassName());
        }
    }

    public List<JavaFile> convertToJavaFiles() {
        List<JavaFile> javaFiles = new ArrayList<>();
        Map<DatabaseActivatorAggregate, JavaFile> databaseMap = new HashMap<>();

        activators
                .databases()
                .forEach(database -> {
                    JavaFile databaseJavaFile = this.generateDatabaseJavaFile(database);

                    javaFiles.add(databaseJavaFile);
                    databaseMap.put(database, databaseJavaFile);
                });

        activators
                .externalEntities()
                .forEach(externalEntity -> javaFiles.add(this.generateExternalEntityJavaFile(activators, externalEntity)));

        activators
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
                ClassName parameterClassName = connector.type();
                methodSpecBuilder.addParameter(parameterClassName, "input" + i);
                i++;
            }

            for (QueryInput queryInput : flowThrough.queries()) {
                ClassName parameterClassName = queryInput.queryInputDefinition().output();
                methodSpecBuilder.addParameter(parameterClassName, "dbInput" + i);
                i++;
            }

            ClassName returnClassName = flowThrough.output().type();
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
                    databaseClassname = database.getQueriesClassName();
                } else if (database.qualifiedName().isPresent()) {
                    databaseClassname = ClassName.bestGuess(database.qualifiedName().get().value());
                } else {
                    databaseClassname = ClassName.bestGuess(dfdPackageName + "." + databaseRequirementsName);
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
                            connector.type(),
                            "input" + j
                    );
                }

                interfaceBuilder.addMethod(queryMethodSpecBuilder.build());
            }
        }

        newFiles.add(JavaFile.builder(dfdPackageName, interfaceBuilder.build()).build());

        return newFiles;
    }

    private JavaFile generateExternalEntityJavaFile(Activators activators, ExternalEntityActivatorAggregate externalEntityActivator) {
        return TraversesGenerator.generateExternalEntityJavaFile(activators, dfdPackageName, externalEntityActivator);
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
                    .returns(databaseActivator.getQueriesClassName())
                    .build();

            databaseSpec.addMethod(getQueriesInstance);
        }

        return JavaFile.builder(dfdPackageName, databaseSpec.build()).build();
    }

    private TypeSpec generateQuery(QueryInputDefinition queryInputDefinition, String queryInterfaceName, ClassName databaseClassname) {
        ClassName returnQueryType = queryInputDefinition.output();

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
                methodSpecBuilder.addParameter(connector.type(), "input" + i);
            }

            outputMethods.add(methodSpecBuilder.build());
        });

        return outputMethods;
    }

    public static AnnotationSpec getGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("\"holt.processor.DFDsProcessor\""))
                .build();
    }
}
