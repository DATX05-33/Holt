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
        for (FlowThroughRep flowThrough : flowThroughReps) {
            TraverseName traverseName = flowThrough.traverseName();
            Flow flow = flowThrough.processActivator().getFlow(traverseName);
            flow.setOutputType(flowThrough.outputType());
            flow.setFunctionName(new FunctionName(flowThrough.functionName()));

            flowThrough.queries().forEach(query -> {
                for (Connector input : flow.inputs()) {
                    if (input instanceof QueryConnector inputQueryConnector) {
                        DatabaseActivatorAggregate databaseActivator = inputQueryConnector.database();
                        if ((databaseActivator.name().value()).equals(query.db().simpleName())) {
                            inputQueryConnector.setType(query.type());
                        }
                    }
                }
            });

            for (QueryDefinitionRep queryDefinitionRep : flowThrough.overrideQueries()) {
                for (QueryConnector queryConnector : queryDefinitionRep.process().queryConnectorsDefinitions()) {
                    if (queryConnector.database().equals(queryDefinitionRep.db())) {
                        queryDefinitionRep.process().removeQueryConnectorDefinition(queryConnector);
                        queryConnector.setQueryDefinition(new QueryDefinition(flowThrough.processActivator(), flow));
                        flowThrough.processActivator().addQueryConnectorDefinition(queryConnector);
                        break;
                    }
                }

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

        for (Flow flow : processActivator.getFlows()) {
            MethodSpec.Builder methodSpecBuilder = MethodSpec
                    .methodBuilder(flow.functionName().value())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

            int i = 0;
            for (Connector connector : flow.inputs()) {
                ClassName parameterClassName = connector.type();

                String parameterName = "input" + i;
                if (connector instanceof QueryConnector) {
                    parameterName = "dbInput" + i;
                }

                methodSpecBuilder.addParameter(parameterClassName, parameterName);
                i++;
            }

            ClassName returnClassName = flow.output().type();
            methodSpecBuilder.returns(returnClassName);

            interfaceBuilder.addMethod(methodSpecBuilder.build());
        }

        // Databases queries definitions
        for (QueryConnector queryInput : processActivator.queryConnectorsDefinitions()) {
            Flow flow = queryInput.queryDefinition().flow();

            // First add query interface
            String databaseRequirementsName = databaseMap.get(queryInput.database()).typeSpec.name;

            // Find what the type of db should be used. Either querier, db or db requirements
            ClassName databaseClassname;
            if (queryInput.database().getQueriesClassName() != null) {
                databaseClassname = queryInput.database().getQueriesClassName();
            } else if (queryInput.database().qualifiedName().isPresent()) {
                databaseClassname = ClassName.bestGuess(queryInput.database().qualifiedName().get().value());
            } else {
                databaseClassname = ClassName.bestGuess(dfdPackageName + "." + databaseRequirementsName);
            }

            String databaseName = queryInput.database().name().value();
            String queryInterfaceName =  databaseName
                    + "To"
                    + processActivator.name().value()
                    + flow.functionName().inPascalCase()
                    + "Query";
            TypeSpec queryInterfaceSpec = generateQuery(queryInput, queryInterfaceName, databaseClassname);
            newFiles.add(JavaFile.builder(dfdPackageName, queryInterfaceSpec).build());

            // Then add method to create that interface
            ClassName returnClass = ClassName.bestGuess(dfdPackageName + "." + queryInterfaceSpec.name);
            String queryMethodName = "query" + databaseName + flow.functionName().inPascalCase();
            MethodSpec.Builder queryMethodSpecBuilder = MethodSpec
                    .methodBuilder(queryMethodName)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(returnClass);

            List<Connector> inputs = new ArrayList<>(flow.inputs());
            TraverseName traverseName = null;
            int processIndex = -1;
            for (Map.Entry<TraverseName, List<ActivatorAggregate>> traverseNameSet : this.activators.traverses().entrySet()) {
                for (ActivatorAggregate activatorAggregate : traverseNameSet.getValue()) {
                    if (activatorAggregate instanceof ProcessActivatorAggregate processActivatorAggregate) {
                        for (Flow processActivatorAggregateFlow : processActivatorAggregate.getFlows()) {
                            if (processActivatorAggregateFlow.inputs().contains(queryInput)) {
                                traverseName = traverseNameSet.getKey();
                                processIndex = traverseNameSet.getValue().indexOf(processActivatorAggregate);
                            }
                        }
                    }
                }
            }

            if (traverseName == null || processIndex == -1) {
                throw new IllegalStateException();
            }

            for (int i = processIndex; i < this.activators.traverses().get(traverseName).size(); i++) {
                ActivatorAggregate activatorAggregate = this.activators.traverses().get(traverseName).get(i);
                if (activatorAggregate instanceof ProcessActivatorAggregate processActivatorAggregate) {
                    inputs.remove(processActivatorAggregate.getFlow(traverseName).output());
                }
            }

            int j = 0;
            for (Connector input2 : inputs) {
                if (!(input2 instanceof QueryConnector)) {
                    ClassName parameterClassName = input2.type();
                    queryMethodSpecBuilder.addParameter(
                            parameterClassName,
                            "input" + j
                    );
                    j++;
                }
            }

            interfaceBuilder.addMethod(queryMethodSpecBuilder.build());
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

        databaseActivator.stores().forEach((flowName, connector) -> {
            ParameterSpec inputParameter = ParameterSpec.builder(connector.type(), "input")
                    .build();

            MethodSpec storeMethod = MethodSpec.methodBuilder(flowName.value())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(inputParameter)
                    .build();

            databaseSpec.addMethod(storeMethod);
        });

        if (databaseActivator.getQueriesClassName() != null) {
            MethodSpec getQueriesInstance = MethodSpec.methodBuilder("getQuerierInstance")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(databaseActivator.getQueriesClassName())
                    .build();

            databaseSpec.addMethod(getQueriesInstance);
        }

        return JavaFile.builder(dfdPackageName, databaseSpec.build()).build();
    }

    private TypeSpec generateQuery(QueryConnector queryConnector, String queryInterfaceName, ClassName databaseClassname) {
        ClassName returnQueryType = queryConnector.type();

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

    public static AnnotationSpec getGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("\"holt.processor.DFDsProcessor\""))
                .build();
    }
}
