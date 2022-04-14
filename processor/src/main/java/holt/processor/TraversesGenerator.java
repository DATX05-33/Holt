package holt.processor;

import com.squareup.javapoet.*;
import holt.processor.activator.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static holt.processor.DFDToJavaFileConverter.getGeneratedAnnotation;
import static holt.processor.DFDsProcessor.EXTERNAL_ENTITY_PREFIX;

public final class TraversesGenerator {

    private TraversesGenerator() {}

    private record State(ExternalEntityActivatorAggregate externalEntityActivator,
                         Activators activators,
                         Map<ActivatorAggregate, String> activatorToVariable,
                         Map<Connector, String> connectorToVariable) {
        public State(ExternalEntityActivatorAggregate externalEntityActivator, Activators activators) {
            this(externalEntityActivator, activators, new HashMap<>(), new HashMap<>());
        }
    }

    public static JavaFile generateExternalEntityJavaFile(Activators activators, String dfdPackageName, ExternalEntityActivatorAggregate externalEntityActivator) {
        State state = new State(externalEntityActivator, activators);

        TypeSpec.Builder externalEntityBuilder = TypeSpec
                .classBuilder(externalEntityActivator.requirementsName().value())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(getGeneratedAnnotation());

        if (activatorsAreValid(activators)) {
            var code = generateFieldsAndConstructor(state);
            externalEntityBuilder.addFields(code.fieldSpecs());
            externalEntityBuilder.addMethod(code.constructorSpec());
            externalEntityBuilder.addMethods(code.getInstanceSpecs());

            externalEntityActivator
                    .starts()
                    .entrySet()
                    .stream()
                    .map(flowNameFlowEntry -> generateTraverse(
                            flowNameFlowEntry.getKey(),
                            flowNameFlowEntry.getValue().output(),
                            state
                    ))
                    .forEach(externalEntityBuilder::addMethod);
        }

        externalEntityActivator.onlyEnds().forEach((traverseName, connector) -> {
            externalEntityBuilder.addMethod(
                    MethodSpec.methodBuilder(traverseName.value())
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .addParameter(connector.type(), "v")
                            .build()
            );
        });

        return JavaFile.builder(dfdPackageName, externalEntityBuilder.build()).build();
    }

    private static boolean activatorsAreValid(Activators activators) {
        return activators.stream()
                .map(activatorAggregates -> activatorAggregates.qualifiedName().isPresent())
                .reduce(true, (b1, b2) -> b1 && b2);
    }

    private record ExternalEntityFieldsWithConstructor(List<FieldSpec> fieldSpecs,
                                                      MethodSpec constructorSpec,
                                                      List<MethodSpec> getInstanceSpecs) { }

    private static ExternalEntityFieldsWithConstructor generateFieldsAndConstructor(State state) {
        var activators = state.activators;
        var connectorToVariable = state.connectorToVariable;

        List<FieldSpec> fieldSpecs = new ArrayList<>();
        MethodSpec.Builder constructorSpecBuilder = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        List<MethodSpec> getInstanceSpecs = new ArrayList<>();

        // All the activators that need instantiation
        List<ActivatorAggregate> activatorsToInstantiate = activators.traverses()
                .entrySet()
                .stream()
                .filter(entrySet ->
                        // Retrieves only activators from traverses where the traverse start with this external entity.
                        entrySet.getValue().get(0).equals(state.externalEntityActivator))
                // Extract all query connectors for their databases activators.
                .<ActivatorAggregate>mapMulti((traverseEntry, consumer) ->
                        traverseEntry.getValue().forEach(activatorAggregate -> {
                                if (activatorAggregate instanceof ProcessActivatorAggregate processActivatorAggregate) {
                                    processActivatorAggregate.getFlow(traverseEntry.getKey()).inputs()
                                            .stream()
                                            .filter(connector -> connector instanceof QueryConnector)
                                            .forEach(connector -> consumer.accept(((QueryConnector) connector).database()));
                                }
                                consumer.accept(activatorAggregate);
                }))
                .distinct()
                .toList();

        int connectorVariableIndex = 0;
        for (ActivatorAggregate activatorAggregate : activatorsToInstantiate) {
            if (!activatorAggregate.equals(state.externalEntityActivator)) {
                var activatorCode = generateCodeForActivator(activatorAggregate, state);
                fieldSpecs.addAll(activatorCode.fieldSpecs);
                constructorSpecBuilder.addCode(activatorCode.instantiation);
                getInstanceSpecs.add(activatorCode.getInstance);
            }

            if (activatorAggregate instanceof ProcessActivatorAggregate processActivator) {
                for (Flow flow : processActivator.getFlows()) {
                    if (!connectorToVariable.containsKey(flow.output())) {
                        connectorToVariable.put(flow.output(), "v" + connectorVariableIndex);
                        connectorVariableIndex++;
                    }
                    for (Connector connector : flow.inputs()) {
                        if (connector instanceof QueryConnector) {
                            connectorToVariable.put(connector, "v" + connectorVariableIndex);
                            connectorVariableIndex++;
                        }
                    }
                }
            } else if (activatorAggregate instanceof ExternalEntityActivatorAggregate externalEntityActivator) {
                for (Flow flow : externalEntityActivator.starts().values()) {
                    if (!connectorToVariable.containsKey(flow.output())) {
                        connectorToVariable.put(flow.output(), "v" + connectorVariableIndex);
                        connectorVariableIndex++;
                    }
                }
            }
        }

        return new ExternalEntityFieldsWithConstructor(
                fieldSpecs,
                constructorSpecBuilder.build(),
                getInstanceSpecs
        );
    }

    // fieldSpec needs to be a list to be able to handle the possibility of Querier for a database.
    private record FieldAndConstructorInstantiation(List<FieldSpec> fieldSpecs,
                                                    CodeBlock instantiation,
                                                    MethodSpec getInstance) { }

    private static FieldAndConstructorInstantiation generateCodeForActivator(ActivatorAggregate activatorAggregate, State state) {
        if (activatorAggregate.qualifiedName().isEmpty()) {
            throw new IllegalStateException("All activators needs to have set a qualified name");
        }

        var activatorToVariable = state.activatorToVariable;
        List<FieldSpec> fieldSpecs = new ArrayList<>();

        String activatorName = activatorAggregate.name().value();
        String activatorQualifiedName = activatorAggregate.qualifiedName().get().value();
        String activatorReferenceVar = activatorName + "Ref";
        activatorToVariable.put(activatorAggregate, activatorReferenceVar);

        ClassName activatorClassName = ClassName.bestGuess(activatorQualifiedName);
        FieldSpec fieldSpec = FieldSpec.builder(
                activatorClassName,
                activatorReferenceVar,
                Modifier.PRIVATE,
                Modifier.FINAL
        ).build();

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        codeBlockBuilder.add("  this." + activatorReferenceVar + " = get" + activatorName + "Instance()" + ";\n");

        MethodSpec getInstance = MethodSpec.methodBuilder("get" + activatorName + "Instance")
                .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
                .returns(activatorClassName)
                .build();

        fieldSpecs.add(fieldSpec);

        if (activatorAggregate instanceof DatabaseActivatorAggregate databaseActivatorAggregate
                && databaseActivatorAggregate.getQueriesClassName() != null) {
            String queriesReferenceVar = databaseActivatorAggregate.getQueriesClassName().simpleName() + "Ref";
            FieldSpec queriesFieldSpec = FieldSpec.builder(
                    databaseActivatorAggregate.getQueriesClassName(),
                    queriesReferenceVar,
                    Modifier.PRIVATE,
                    Modifier.FINAL
            ).build();

            codeBlockBuilder.add("  this." + queriesReferenceVar + " = " + activatorReferenceVar + ".getQuerierInstance();\n");

            fieldSpecs.add(queriesFieldSpec);
        }

        return new FieldAndConstructorInstantiation(
                fieldSpecs,
                codeBlockBuilder.build(),
                getInstance
        );
    }

    private static MethodSpec generateTraverse(TraverseName traverseName, Connector input, State state) {
        var orderOfExecution = state.activators.traverses().get(traverseName);
        var connectorToVariable = state.connectorToVariable;
        var activatorToVariable = state.activatorToVariable;

        ClassName parameterClassType = input.type();
        String first;
        if (orderOfExecution.size() == 0) {
            first = "d";
        } else {
            first = connectorToVariable.get(input);
        }

        ParameterSpec dataParameterSpec = ParameterSpec.builder(parameterClassType, first).build();

        MethodSpec.Builder methodSpecBuilder = MethodSpec
                .methodBuilder(traverseName.value())
                .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                .addParameter(dataParameterSpec);

        ActivatorAggregate firstActivatorAggregate = orderOfExecution.get(0);

        if (!(firstActivatorAggregate instanceof ExternalEntityActivatorAggregate)) {
            throw new IllegalStateException("First activator aggregate must be an external entity activator aggregate");
        }

        // Every processor except the first.
        for (int i = 1; i < orderOfExecution.size(); i++) {
            ActivatorAggregate activatorAggregate = orderOfExecution.get(i);
            boolean lastActivator = i == orderOfExecution.size() - 1;

            if (lastActivator && firstActivatorAggregate.equals(activatorAggregate)) {
                ExternalEntityActivatorAggregate externalEntityActivator =
                        (ExternalEntityActivatorAggregate) activatorAggregate;
                Connector lastConnector = externalEntityActivator.end(traverseName)
                        .orElseThrow(IllegalStateException::new);

                String returnVariable = connectorToVariable.get(lastConnector);

                CodeBlock returnStatement = CodeBlock.builder().add("return " + returnVariable + ";").build();
                methodSpecBuilder.addCode(returnStatement);
                ClassName returnClassType = lastConnector.type();
                methodSpecBuilder.returns(returnClassType);
            } else if (activatorAggregate instanceof ProcessActivatorAggregate processActivator) {
                Flow flow = processActivator.getFlow(traverseName);
                String connectorVar = connectorToVariable.get(processActivator.getOutput(traverseName));
                String activatorReferenceVar = activatorToVariable.get(activatorAggregate);
                FunctionName functionName = flow.functionName();

                StringBuilder processorCallSB = new StringBuilder();

                // First, check if this process has any queries
                // If it does, then call for all the query definitions
                flow.inputs()
                        .stream()
                        .filter(connector -> connector instanceof QueryConnector)
                        .map(connector -> (QueryConnector) connector)
                        .forEach(queryConnector -> {
                            QueryDefinition queryDefinition = queryConnector.queryDefinition();
                            String activatorThatHaveQueryDefinitionVar = activatorToVariable.get(queryDefinition.source());
                            Flow queryDefinitionFlow = queryConnector.queryDefinition().flow();
                            processorCallSB.append("var ")
                                    .append(connectorToVariable.get(queryConnector))
                                    .append(" = ")
                                    .append("this.")
                                    .append(activatorThatHaveQueryDefinitionVar)
                                    .append(".query")
                                    .append(queryConnector.database().name().value())
                                    .append(queryDefinitionFlow.functionName().inPascalCase())
                                    .append("(");

                            int queryInputs = 0;

                            List<Connector> inputs = new ArrayList<>(flow.inputs());
                            int processIndex = -1;
                            for (ActivatorAggregate _activatorAggregate : orderOfExecution) {
                                if (_activatorAggregate instanceof ProcessActivatorAggregate processActivatorAggregate) {
                                    for (Flow processActivatorAggregateFlow : processActivatorAggregate.getFlows()) {
                                        if (processActivatorAggregateFlow.inputs().contains(queryConnector)) {
                                            processIndex = orderOfExecution.indexOf(processActivatorAggregate);
                                        }
                                    }
                                }
                            }

                            if (processIndex == -1) {
                                throw new IllegalStateException();
                            }

                            boolean debug = state.externalEntityActivator.name().equals(new ActivatorName("Company"));
                            if (debug) {
                                System.out.println("before");
                                inputs.forEach(System.out::println);
                            }

                            for (int j = 0; j < processIndex; j++) {
                                ActivatorAggregate _activatorAggregate = orderOfExecution.get(j);
                                if (_activatorAggregate instanceof ProcessActivatorAggregate processActivatorAggregate) {
                                    boolean removed = inputs.remove(processActivatorAggregate.getFlow(traverseName).output());
                                    if (debug) {
                                        System.out.println("[" + j + "] - " + processActivatorAggregate.name() + " { " + processActivatorAggregate.getOutput(traverseName).type() + "} ? " + removed);
                                    }
                                }
                            }

                            if (debug) {
                                System.out.println("after");
                                inputs.forEach(System.out::println);
                            }

                            for (Connector connectorInput : inputs) {
                                if (!(connectorInput instanceof QueryConnector)) {
                                    queryInputs++;
                                    processorCallSB.append(connectorToVariable.get(connectorInput))
                                            .append(",");
                                }
                            }

                            //Removes the last , from the previous forEach, if there was any input to the query
                            if (queryInputs > 0) {
                                processorCallSB.setLength(processorCallSB.length() - 1);
                            }

                            processorCallSB.append(");\n");
                        });

                processorCallSB.append("var ")
                        .append(connectorVar)
                        .append(" = this.")
                        .append(activatorReferenceVar)
                        .append(".")
                        .append(functionName)
                        .append("(");
                for (Connector connector : flow.inputs()) {
                    // Query all the relevant information for the process
                    if (connector instanceof QueryConnector queryConnector) {
                        String queryDefinitionVar = connectorToVariable.get(queryConnector);

                        String querier;
                        //Either it's the querier for the db, or it's the db.
                        if (queryConnector.database().getQueriesClassName() != null) {
                            querier = queryConnector.database().getQueriesClassName().simpleName() + "Ref";
                        } else {
                            querier = queryConnector.database().name().value() + "Ref";
                        }

                        processorCallSB
                                .append(queryDefinitionVar)
                                .append(".createQuery(this.")
                                .append(querier)
                                .append("),");
                    } else {
                        processorCallSB.append(connectorToVariable.get(connector))
                                .append(",");
                    }
                }
                // Removes the last ,
                processorCallSB.setLength(processorCallSB.length() - 1);
                processorCallSB.append(");\n");

                methodSpecBuilder.addCode(CodeBlock.of(processorCallSB.toString()));
            } else if (activatorAggregate instanceof DatabaseActivatorAggregate databaseActivatorAggregate) {
                String activatorReferenceVar = activatorToVariable.get(activatorAggregate);
                Connector storeConnector = databaseActivatorAggregate.getStore(traverseName);

                StringBuilder databaseCallSB = new StringBuilder();
                databaseCallSB
                        .append("this.")
                        .append(activatorReferenceVar)
                        .append(".")
                        .append(traverseName.value())
                        .append("(")
                        .append(connectorToVariable.get(storeConnector))
                        .append(");\n");

                methodSpecBuilder.addCode(CodeBlock.of(databaseCallSB.toString()));
            } else if (activatorAggregate instanceof ExternalEntityActivatorAggregate externalEntityActivator) {
                String activatorReferenceVar = activatorToVariable.get(activatorAggregate);
                Connector lastConnector = externalEntityActivator.end(traverseName)
                        .orElseThrow(IllegalStateException::new);

                StringBuilder externalEntitySB = new StringBuilder();
                externalEntitySB
                        .append("this.")
                        .append(activatorReferenceVar)
                        .append(".")
                        .append(traverseName.value())
                        .append("(")
                        .append(connectorToVariable.get(lastConnector))
                        .append(");\n");

                methodSpecBuilder.addCode(CodeBlock.of(externalEntitySB.toString()));
            }
        }

        return methodSpecBuilder.build();
    }

}
