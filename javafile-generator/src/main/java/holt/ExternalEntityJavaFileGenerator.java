package holt;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import holt.activator.ActivatorAggregate;
import holt.activator.ConnectedClass;
import holt.activator.Connector;
import holt.activator.DatabaseActivatorAggregate;
import holt.activator.Domain;
import holt.activator.ExternalEntityActivatorAggregate;
import holt.activator.FlowThroughAggregate;
import holt.activator.FunctionName;
import holt.activator.OutputActivator;
import holt.activator.ProcessActivatorAggregate;
import holt.activator.QueryInput;
import holt.activator.QueryInputDefinition;
import holt.activator.TraverseName;
import holt.activator.TraverseOutput;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static holt.JavaFileGenerator.getGeneratedAnnotation;
import static holt.JavaFileGenerator.toTypeName;

public final class ExternalEntityJavaFileGenerator {

    private ExternalEntityJavaFileGenerator() {}

    private record State(ExternalEntityActivatorAggregate externalEntityActivator,
                         Domain domain,
                         Map<ActivatorAggregate, String> activatorToVariable,
                         Map<Connector, String> connectorToVariable,
                         Map<QueryInputDefinition, String> queryInputDefinitionToVariable) {
        public State(ExternalEntityActivatorAggregate externalEntityActivator, Domain domain) {
            this(externalEntityActivator, domain, new HashMap<>(), new HashMap<>(), new HashMap<>());
        }
    }

    public static JavaFile generate(Domain domain, String dfdPackageName, ExternalEntityActivatorAggregate externalEntityActivator) {
        State state = new State(externalEntityActivator, domain);

        TypeSpec.Builder externalEntityBuilder = TypeSpec
                .classBuilder(externalEntityActivator.requirementsName().value())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(getGeneratedAnnotation());

        if (activatorsAreValid(domain)) {
            var code = generateFieldsAndConstructor(state);
            externalEntityBuilder.addFields(code.fieldSpecs());
            externalEntityBuilder.addMethod(code.constructorSpec());
            externalEntityBuilder.addMethods(code.methods());

            externalEntityActivator
                    .starts()
                    .entrySet()
                    .stream()
                    .map(traverseNameConnectorEntry -> generateTraverse(
                            traverseNameConnectorEntry.getKey(),
                            traverseNameConnectorEntry.getValue(),
                            state
                    ))
                    .forEach(externalEntityBuilder::addMethod);
        }

        List<MethodSpec> outputMethods = JavaFileGenerator.toOutputMethods(new ArrayList<>(externalEntityActivator.outputs().values()));
        externalEntityBuilder.addMethods(outputMethods);

        return JavaFile.builder(dfdPackageName, externalEntityBuilder.build()).build();
    }

    private static boolean activatorsAreValid(Domain domain) {
        for (ActivatorAggregate activator : domain.activators()) {
            if (activator.connectedClass().isEmpty()) {
                System.out.println(activator.requirementsName() + " have no connected class to it");
            }
        }

        return domain.activators().stream()
                .map(activatorAggregates -> activatorAggregates.connectedClass().isPresent())
                .reduce(true, (b1, b2) -> b1 && b2);
    }

    private record ExternalEntityFieldsWithConstructor(List<FieldSpec> fieldSpecs,
                                                      MethodSpec constructorSpec,
                                                      List<MethodSpec> methods) { }

    private static ExternalEntityFieldsWithConstructor generateFieldsAndConstructor(State state) {
        var activators = state.domain;
        var connectorToVariable = state.connectorToVariable;
        var queryInputDefinitionToVariable = state.queryInputDefinitionToVariable;

        List<FieldSpec> fieldSpecs = new ArrayList<>();
        MethodSpec.Builder constructorSpecBuilder = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        List<MethodSpec> methods = new ArrayList<>();

        // All the activators that need instantiation
        List<ActivatorAggregate> activatorsToInstantiate = activators.traverses()
                .entrySet()
                .stream()
                .filter(entrySet ->
                        // Retrieves only activators from traverses where the traverse start with this external entity.
                        entrySet.getValue().get(0).equals(state.externalEntityActivator)
                )
                // Also extract queries for their databases activators.
                .<ActivatorAggregate>mapMulti((traverseEntry, consumer) ->
                        traverseEntry.getValue().forEach(activatorAggregate -> {
                            if (activatorAggregate instanceof ProcessActivatorAggregate processActivatorAggregate) {
                                processActivatorAggregate.flow(traverseEntry.getKey())
                                        .queryInputDefinitions()
                                        .stream()
                                        .map(QueryInputDefinition::database)
                                        .forEach(consumer::accept);
                            }
                            consumer.accept(activatorAggregate);
                }))
                .distinct()
                .toList();

        int connectorVariableIndex = 0;

        List<ConnectedClass> classes = new ArrayList<>();

        boolean needToGenerateReflectionHelperMethod = false;
        for (ActivatorAggregate activatorAggregate : activatorsToInstantiate) {
            if (activatorAggregate instanceof ProcessActivatorAggregate processActivator) {
                for (FlowThroughAggregate flowThroughAggregate : processActivator.flows()) {
                    if (!connectorToVariable.containsKey(flowThroughAggregate.output())) {
                        connectorToVariable.put(flowThroughAggregate.output(), "v" + connectorVariableIndex);
                        connectorVariableIndex++;
                    }
                    for (QueryInputDefinition queryInputDefinition : flowThroughAggregate.queryInputDefinitions()) {
                        queryInputDefinitionToVariable.put(queryInputDefinition, "qd" + connectorVariableIndex);
                        connectorVariableIndex++;
                    }
                }
            }

            if (!activatorAggregate.equals(state.externalEntityActivator)) {
                String activatorVariableRef = activatorAggregate.name().asVariableName();
                String activatorReferenceVar = activatorVariableRef + "Ref";
                state.activatorToVariable.put(activatorAggregate, activatorReferenceVar);

                if (classes.contains(activatorAggregate.connectedClass().get())) {
                    continue;
                }

                classes.add(activatorAggregate.connectedClass().get());

                var activatorCode = generateCodeForActivator(activatorAggregate, state);
                fieldSpecs.addAll(activatorCode.fieldSpecs);
                constructorSpecBuilder.addCode(activatorCode.instantiation);

                // If true, then activatorcode.instantiation contains code that uses a reflection helper
                // method to generate the instance for activators
                if (activatorCode.parameterSpec == null) {
                    needToGenerateReflectionHelperMethod = true;
                } else {
                    parameterSpecs.add(activatorCode.parameterSpec);
                }
            }
        }

        if (needToGenerateReflectionHelperMethod) {
            TypeVariableName t = TypeVariableName.get("T");

            MethodSpec.Builder reflectionHelperMethodBuilder = MethodSpec.methodBuilder("reflect")
                    .addModifiers(Modifier.PRIVATE)
                    .addTypeVariable(t)
                    .addParameter(ClassName.bestGuess("Class<?>"), "t")
                    .returns(t);

            CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
            codeBlockBuilder.add("  try {\n");
            codeBlockBuilder.add("    return (T) t.getDeclaredConstructor().newInstance();\n");
            codeBlockBuilder.add("  } catch (Exception e) {\n");
            codeBlockBuilder.add("    e.printStackTrace();\n");
            codeBlockBuilder.add("    throw new IllegalStateException(\"Could not instantiate...\");\n");
            codeBlockBuilder.add("  }");
            codeBlockBuilder.add("\n");

            reflectionHelperMethodBuilder.addCode(codeBlockBuilder.build());

            methods.add(reflectionHelperMethodBuilder.build());
        }

        constructorSpecBuilder.addParameters(parameterSpecs);

        return new ExternalEntityFieldsWithConstructor(
                fieldSpecs,
                constructorSpecBuilder.build(),
                methods
        );
    }

    // fieldSpec needs to be a list to be able to handle the possibility of Querier for a database.
    private record FieldAndConstructorInstantiation(List<FieldSpec> fieldSpecs,
                                                    CodeBlock instantiation,
                                                    ParameterSpec parameterSpec) { }

    private static FieldAndConstructorInstantiation generateCodeForActivator(ActivatorAggregate activatorAggregate, State state) {
        if (activatorAggregate.connectedClass().isEmpty()) {
            throw new IllegalStateException("All activators needs to have set a qualified name");
        }
        ConnectedClass connectedClass = activatorAggregate.connectedClass().get();

        var activatorToVariable = state.activatorToVariable;
        List<FieldSpec> fieldSpecs = new ArrayList<>();

        String activatorReferenceVar = activatorToVariable.get(activatorAggregate);

        TypeName activatorClassName = toTypeName(connectedClass.qualifiedName());
        FieldSpec fieldSpec = FieldSpec.builder(
                activatorClassName,
                activatorReferenceVar,
                Modifier.PRIVATE,
                Modifier.FINAL
        ).build();

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        ParameterSpec parameterSpec = null;
        if (!connectedClass.instantiateWithReflection()) {
            parameterSpec = ParameterSpec.builder(activatorClassName, activatorAggregate.name().asVariableName())
                    .build();

            codeBlockBuilder.add("  this." + activatorReferenceVar + " = " + activatorAggregate.name().asVariableName() + ";\n");
        } else {
            codeBlockBuilder.add("  this." + activatorReferenceVar + " = reflect(" + activatorClassName + ".class)" + ";\n");
        }

        fieldSpecs.add(fieldSpec);

        if (activatorAggregate instanceof DatabaseActivatorAggregate databaseActivatorAggregate
                && databaseActivatorAggregate.getQueriesClassName() != null) {
            String queriesReferenceVar = databaseActivatorAggregate.getQueriesClassName().simpleName() + "Ref";
            queriesReferenceVar = queriesReferenceVar.substring(0, 1).toLowerCase() + queriesReferenceVar.substring(1);
            FieldSpec queriesFieldSpec = FieldSpec.builder(
                    toTypeName(databaseActivatorAggregate.getQueriesClassName()),
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
                parameterSpec
        );
    }

    private static MethodSpec generateTraverse(TraverseName traverseName, List<Connector> inputs, State state) {
        var orderOfExecution = state.domain.traverses().get(traverseName);
        var connectorToVariable = state.connectorToVariable;
        var activatorToVariable = state.activatorToVariable;
        var queryInputDefinitionToVariable = state.queryInputDefinitionToVariable;

        List<QueryInputDefinition> queryInputDefinitionDefined = new ArrayList<>();

        List<ParameterSpec> inputParameters = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            Connector input = inputs.get(i);
            String varName = "input" + i;
            inputParameters.add(
                    ParameterSpec
                            .builder(toTypeName(input), varName)
                            .build()
            );

            connectorToVariable.put(input, varName);
        }

        MethodSpec.Builder methodSpecBuilder = MethodSpec
                .methodBuilder(traverseName.value())
                .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                .addParameters(inputParameters);

        ActivatorAggregate firstActivatorAggregate = orderOfExecution.get(0);

        if (!(firstActivatorAggregate instanceof ExternalEntityActivatorAggregate) ) {
            throw new IllegalStateException("First activator aggregate must be an external entity activator aggregate");
        }

        // Every processor except the first.
        for (int i = 1; i < orderOfExecution.size(); i++) {
            ActivatorAggregate activatorAggregate = orderOfExecution.get(i);
            boolean lastActivator = i == orderOfExecution.size() - 1;

            if (lastActivator && firstActivatorAggregate.equals(activatorAggregate)) {
                ExternalEntityActivatorAggregate externalEntityActivator =
                        (ExternalEntityActivatorAggregate) activatorAggregate;
                TraverseOutput lastTraverseOutput = externalEntityActivator.end(traverseName)
                        .orElseThrow(IllegalStateException::new);

                if (lastTraverseOutput.inputs().size() != 1) {
                    throw new IllegalStateException("Last traverse output that goes back to the start must only have one input (return limitations)");
                }
                Connector lastConnector = lastTraverseOutput.inputs().get(0);

                String returnVariable = connectorToVariable.get(lastConnector);

                CodeBlock returnStatement = CodeBlock.builder().add("return " + returnVariable + ";").build();
                methodSpecBuilder.addCode(returnStatement);
                TypeName returnClassType = toTypeName(lastConnector);
                methodSpecBuilder.returns(returnClassType);
            } else if (activatorAggregate instanceof ProcessActivatorAggregate processActivator) {
                FlowThroughAggregate flowThrough = processActivator.flow(traverseName);
                String connectorVar = connectorToVariable.get(processActivator.getOutput(traverseName));
                String activatorReferenceVar = activatorToVariable.get(activatorAggregate);
                FunctionName functionName = flowThrough.functionName();

                StringBuilder processorCallSB = new StringBuilder();

                // First, check if this process has any queries
                // If it does, then call for all the query definitions

                int finalI = i;
                for (QueryInput query : flowThrough.queries()) {
                    QueryInputDefinition queryInputDefinition = query.queryInputDefinition();
                    if (queryInputDefinitionDefined.contains(queryInputDefinition)) {
                        throw new IllegalStateException();
                    }
                    queryInputDefinitionDefined.add(queryInputDefinition);
                    codeForQueryInputDefinition(
                            traverseName,
                            orderOfExecution,
                            connectorToVariable,
                            queryInputDefinitionToVariable,
                            processorCallSB,
                            finalI,
                            queryInputDefinition,
                            activatorToVariable
                    );
                }

                processorCallSB.append("final var ")
                        .append(connectorVar)
                        .append(" = this.")
                        .append(activatorReferenceVar)
                        .append(".")
                        .append(functionName)
                        .append("(");
                for (Connector connector : flowThrough.inputs()) {
                        processorCallSB.append(connectorToVariable.get(connector)).append(",");
                }

                for (QueryInput queryInput : flowThrough.queries()) {
                    String queryDefinitionVar = queryInputDefinitionToVariable.get(queryInput.queryInputDefinition());
                    DatabaseActivatorAggregate database = queryInput.queryInputDefinition().database();

                    String querier;
                    //Either it's the querier for the db, or it's the db.
                    if (database.getQueriesClassName() != null) {
                        querier = database.getQueriesClassName().simpleName() + "Ref";
                        querier = querier.substring(0, 1).toLowerCase() + querier.substring(1);
                    } else {
                        querier = database.name().asVariableName() + "Ref";
                    }

                    processorCallSB
                            .append(queryDefinitionVar)
                            .append(".createQuery(this.")
                            .append(querier)
                            .append("),");
                }
                // Removes the last ,
                if (flowThrough.inputs().size() + flowThrough.queries().size() > 0) {
                    processorCallSB.setLength(processorCallSB.length() - 1);
                }

                processorCallSB.append(");\n");
                methodSpecBuilder.addCode(CodeBlock.of(processorCallSB.toString()));
            } else if (activatorAggregate instanceof OutputActivator outputActivator) {
                String activatorReferenceVar = activatorToVariable.get(activatorAggregate);
                TraverseOutput traverseOutput = outputActivator.outputs().get(traverseName);

                StringBuilder traverseSB = new StringBuilder();
                traverseSB
                        .append("this.")
                        .append(activatorReferenceVar)
                        .append(".")
                        .append(traverseName.value())
                        .append("(");

                for (Connector traverseOutputConnector : traverseOutput.inputs()) {
                    traverseSB
                            .append(connectorToVariable.get(traverseOutputConnector))
                            .append(",");
                }

                traverseSB.setLength(traverseSB.length() - 1);
                traverseSB.append(");\n");

                methodSpecBuilder.addCode(CodeBlock.of(traverseSB.toString()));
            }
        }

        return methodSpecBuilder.build();
    }

    private static void codeForQueryInputDefinition(TraverseName traverseName,
                                                    List<ActivatorAggregate> activatorAggregates,
                                                    Map<Connector, String> connectorToVariable,
                                                    Map<QueryInputDefinition, String> queryInputDefinitionToVariable,
                                                    StringBuilder processorCallSB,
                                                    int finalI,
                                                    QueryInputDefinition queryInputDefinition,
                                                    Map<ActivatorAggregate, String> activatorToVariable) {
        ProcessActivatorAggregate queryInputDefinitionProcessActivatorAggregate = getActivatorAggregateByQueryInputDefinition(queryInputDefinition, traverseName, activatorAggregates);
        String activatorReferenceVar = activatorToVariable.get(queryInputDefinitionProcessActivatorAggregate);
        FlowThroughAggregate flowThrough = queryInputDefinitionProcessActivatorAggregate.flow(traverseName);
        processorCallSB.append("final var ")
                .append(queryInputDefinitionToVariable.get(queryInputDefinition))
                .append(" = ")
                .append("this.")
                .append(activatorReferenceVar)
                .append(".")
                .append(queryInputDefinitionProcessActivatorAggregate.getQueryMethodNameForDatabase(queryInputDefinition.database(), flowThrough))
                .append("(");

        List<Connector> queryDefinitionInputs = flowThrough.inputs();
        for (Connector queryDefinitionInputConnector : queryDefinitionInputs) {
            ActivatorAggregate a = getActivatorAggregateByOutputConnector(queryDefinitionInputConnector, traverseName, activatorAggregates);
            int j = activatorAggregates.indexOf(a);
            if (j > finalI) {
                continue;
            }
            processorCallSB.append(connectorToVariable.get(queryDefinitionInputConnector)).append(",");
        }

        //Removes the last , from the previous forEach, if there was any input to the query
        if (queryDefinitionInputs.size() > 0) {
            processorCallSB.setLength(processorCallSB.length() - 1);
        }

        processorCallSB.append(");\n");
    }

    private static ActivatorAggregate getActivatorAggregateByOutputConnector(Connector connector, TraverseName traverseName, List<ActivatorAggregate> activatorAggregates) {
        for (ActivatorAggregate activatorAggregate : activatorAggregates) {
            if (activatorAggregate instanceof ProcessActivatorAggregate processActivatorAggregate) {
                if (processActivatorAggregate.flow(traverseName).output().equals(connector)) {
                    return processActivatorAggregate;
                }
            } else if (activatorAggregate instanceof ExternalEntityActivatorAggregate externalEntityActivatorAggregate) {
                if (externalEntityActivatorAggregate.starts().get(traverseName).contains(connector)) {
                    return externalEntityActivatorAggregate;
                }
            }
        }
        throw new IllegalStateException("Nope");
    }

    private static ProcessActivatorAggregate getActivatorAggregateByQueryInputDefinition(QueryInputDefinition queryInputDefinition,
                                                                                         TraverseName traverseName,
                                                                                         List<ActivatorAggregate> activatorAggregates) {
        for (ActivatorAggregate activatorAggregate : activatorAggregates) {
            if (activatorAggregate instanceof ProcessActivatorAggregate processActivatorAggregate) {
                for (QueryInputDefinition possibleQueryInputDefinition : processActivatorAggregate.flow(traverseName).queryInputDefinitions()) {
                    if (queryInputDefinition.equals(possibleQueryInputDefinition)) {
                        return processActivatorAggregate;
                    }
                }
            }
        }

        throw new IllegalStateException();
    }

}
