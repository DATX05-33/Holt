package holt;

import com.squareup.javapoet.*;
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
import static holt.JavaFileGenerator.toClassName;

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
        for (ActivatorAggregate activatorAggregate : activatorsToInstantiate) {
            boolean needToGenerateReflectionHelperMethod = false;

            if (!activatorAggregate.equals(state.externalEntityActivator)) {
                var activatorCode = generateCodeForActivator(activatorAggregate, state);
                fieldSpecs.addAll(activatorCode.fieldSpecs);
                constructorSpecBuilder.addCode(activatorCode.instantiation);

                // If true, then activatorcode.instantiation contains code that uses a reflection helper
                // method to generate the instance for activators
                if (activatorCode.getInstance == null) {
                    needToGenerateReflectionHelperMethod = true;
                } else {
                    methods.add(activatorCode.getInstance);
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
                codeBlockBuilder.add("    return (T) t.newInstance();\n");
                codeBlockBuilder.add("  } catch (Exception e) {\n");
                codeBlockBuilder.add("    e.printStackTrace();\n");
                codeBlockBuilder.add("    throw new IllegalStateException(\"Could not instantiate...\");\n");
                codeBlockBuilder.add("  }");
                codeBlockBuilder.add("\n");

                reflectionHelperMethodBuilder.addCode(codeBlockBuilder.build());

                methods.add(reflectionHelperMethodBuilder.build());
            }

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
            } else if (activatorAggregate instanceof ExternalEntityActivatorAggregate externalEntityActivator) {
                for (Connector startConnector : externalEntityActivator.starts().values()) {
                    if (!connectorToVariable.containsKey(startConnector)) {
                        connectorToVariable.put(startConnector, "v" + connectorVariableIndex);
                        connectorVariableIndex++;
                    }
                }
            }
        }

        return new ExternalEntityFieldsWithConstructor(
                fieldSpecs,
                constructorSpecBuilder.build(),
                methods
        );
    }

    // fieldSpec needs to be a list to be able to handle the possibility of Querier for a database.
    private record FieldAndConstructorInstantiation(List<FieldSpec> fieldSpecs,
                                                    CodeBlock instantiation,
                                                    MethodSpec getInstance) { }

    private static FieldAndConstructorInstantiation generateCodeForActivator(ActivatorAggregate activatorAggregate, State state) {
        if (activatorAggregate.connectedClass().isEmpty()) {
            throw new IllegalStateException("All activators needs to have set a qualified name");
        }
        ConnectedClass connectedClass = activatorAggregate.connectedClass().get();

        var activatorToVariable = state.activatorToVariable;
        List<FieldSpec> fieldSpecs = new ArrayList<>();

        String activatorVariableRef = activatorAggregate.name().asVariableName();
        String activatorReferenceVar = activatorVariableRef + "Ref";
        activatorToVariable.put(activatorAggregate, activatorReferenceVar);

        ClassName activatorClassName = toClassName(connectedClass.qualifiedName());
        FieldSpec fieldSpec = FieldSpec.builder(
                activatorClassName,
                activatorReferenceVar,
                Modifier.PRIVATE,
                Modifier.FINAL
        ).build();

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();


        MethodSpec getInstance = null;

        if (!connectedClass.instantiateWithReflection()) {
            getInstance = MethodSpec.methodBuilder("get" + activatorAggregate.name().value() + "Instance")
                    .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
                    .returns(activatorClassName)
                    .build();

            codeBlockBuilder.add("  this." + activatorReferenceVar + " = get" + activatorAggregate.name().value() + "Instance()" + ";\n");
        } else {
            codeBlockBuilder.add("  this." + activatorReferenceVar + " = reflect(" + activatorClassName + ".class)" + ";\n");
        }

        fieldSpecs.add(fieldSpec);

        if (activatorAggregate instanceof DatabaseActivatorAggregate databaseActivatorAggregate
                && databaseActivatorAggregate.getQueriesClassName() != null) {
            String queriesReferenceVar = databaseActivatorAggregate.getQueriesClassName().simpleName() + "Ref";
            queriesReferenceVar = queriesReferenceVar.substring(0, 1).toLowerCase() + queriesReferenceVar.substring(1);
            FieldSpec queriesFieldSpec = FieldSpec.builder(
                    toClassName(databaseActivatorAggregate.getQueriesClassName()),
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
        var orderOfExecution = state.domain.traverses().get(traverseName);
        var connectorToVariable = state.connectorToVariable;
        var activatorToVariable = state.activatorToVariable;
        var queryInputDefinitionToVariable = state.queryInputDefinitionToVariable;

        ClassName parameterClassType = toClassName(input.type());
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
                TraverseOutput lastTraverseOutput = externalEntityActivator.end(traverseName)
                        .orElseThrow(IllegalStateException::new);

                if (lastTraverseOutput.inputs().size() != 1) {
                    throw new IllegalStateException("Last traverse output that goes back to the start must only have one input (return limitations)");
                }
                Connector lastConnector = lastTraverseOutput.inputs().get(0);

                String returnVariable = connectorToVariable.get(lastConnector);

                CodeBlock returnStatement = CodeBlock.builder().add("return " + returnVariable + ";").build();
                methodSpecBuilder.addCode(returnStatement);
                ClassName returnClassType = toClassName(lastConnector.type());
                methodSpecBuilder.returns(returnClassType);
            } else if (activatorAggregate instanceof ProcessActivatorAggregate processActivator) {
                FlowThroughAggregate flowThrough = processActivator.flow(traverseName);
                String connectorVar = connectorToVariable.get(processActivator.getOutput(traverseName));
                String activatorReferenceVar = activatorToVariable.get(activatorAggregate);
                FunctionName functionName = flowThrough.functionName();

                StringBuilder processorCallSB = new StringBuilder();

                // First, check if this process has any queries
                // If it does, then call for all the query definitions

                flowThrough.queryInputDefinitions()
                                .forEach(queryInputDefinition -> {
                                    processorCallSB.append("final var ")
                                            .append(queryInputDefinitionToVariable.get(queryInputDefinition))
                                            .append(" = ")
                                            .append("this.")
                                            .append(activatorReferenceVar)
                                            .append(".")
                                            .append(processActivator.getQueryMethodNameForDatabase(queryInputDefinition.database(), flowThrough))
                                            .append("(");

                                    List<Connector> queryInputs = flowThrough.inputs();
                                    for (Connector queryInput : queryInputs) {
                                        processorCallSB.append(connectorToVariable.get(queryInput)).append(",");
                                    }

                                    //Removes the last , from the previous forEach, if there was any input to the query
                                    if (queryInputs.size() > 0) {
                                        processorCallSB.setLength(processorCallSB.length() - 1);
                                    }

                                    processorCallSB.append(");\n");
                                });

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

}
