package holt.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import holt.processor.activator.Activator;
import holt.processor.activator.Activators;
import holt.processor.activator.Connector;
import holt.processor.activator.DatabaseActivator;
import holt.processor.activator.ExternalEntityActivator;
import holt.processor.activator.Flow;
import holt.processor.activator.FlowName;
import holt.processor.activator.ProcessActivator;
import holt.processor.activator.QueryConnector;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TraversesGenerator {

    private Map<Activator, String> activatorToVariable;
    private Map<Connector, String> connectorToVariable;

    public TraversesGenerator() {
        this.activatorToVariable = new HashMap<>();
        this.connectorToVariable = new HashMap<>();
    }

    public record ExternalEntityFieldsWithConstructor(List<FieldSpec> fieldSpecs,
                                                      MethodSpec constructorSpec) { }

    public ExternalEntityFieldsWithConstructor generateFieldsAndConstructor(Activators activators) {
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        MethodSpec.Builder constructorSpecBuilder = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        if (activators.flows().values().size() > 0) {
            constructorSpecBuilder.addCode(CodeBlock.of("try {\n"));
        }

        // Setup variables names


        // All the activators that need instantiation
        List<Activator> activatorsToInstansiate = activators.flows().values()
                .stream()
                .flatMap(Collection::stream)
                .distinct()
                .toList();

        int connectorVariableIndex = 0;
        for (Activator activator : activatorsToInstansiate) {
            if (activator instanceof ProcessActivator || activator instanceof DatabaseActivator) {
                var activatorCode = generateCodeForActivator(activator);
                fieldSpecs.add(activatorCode.fieldSpec);
                constructorSpecBuilder.addCode(activatorCode.instantiation);
            }

            if (activator instanceof ProcessActivator processActivator) {
                for (Flow flow : processActivator.getFlows()) {
                    if (!connectorToVariable.containsKey(flow.output())) {
                        connectorToVariable.put(flow.output(), "v" + connectorVariableIndex);
                        connectorVariableIndex++;
                    }
                }
            } else if (activator instanceof ExternalEntityActivator externalEntityActivator) {
                for (Flow flow : externalEntityActivator.starts().values()) {
                    if (!connectorToVariable.containsKey(flow.output())) {
                        connectorToVariable.put(flow.output(), "v" + connectorVariableIndex);
                        connectorVariableIndex++;
                    }
                }
            }
        }

        if (activators.flows().values().size() > 0) {
            constructorSpecBuilder.addCode(CodeBlock.of("} catch (ClassNotFoundException | IllegalAccessException |  InstantiationException e) {\n"));
            constructorSpecBuilder.addCode(CodeBlock.of("   e.printStackTrace();\n"));
            constructorSpecBuilder.addCode(CodeBlock.of("   throw new IllegalStateException();\n}\n"));
        }

        return new ExternalEntityFieldsWithConstructor(
                fieldSpecs,
                constructorSpecBuilder.build()
        );
    }

    private record FieldAndConstructorInstantiation(FieldSpec fieldSpec,
                                                    CodeBlock instantiation) { }

    private FieldAndConstructorInstantiation generateCodeForActivator(Activator activator) {
        String activatorName = activator.name().value();
        //TODO: Please no
        String activatorPackageName = "holt.test.friend.";
        String activatorReferenceVar = activatorName + "Ref";
        activatorToVariable.put(activator, activatorReferenceVar);

        ClassName activatorClassName = ClassName.bestGuess(activatorPackageName + activatorName);
        FieldSpec fieldSpec = FieldSpec.builder(
                activatorClassName,
                activatorReferenceVar,
                Modifier.PRIVATE,
                Modifier.FINAL
        ).build();

        String activatorClassVar = activatorName + "_";
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        codeBlockBuilder.add("  Class " + activatorClassVar + " = Class.forName(\"" + activatorPackageName + activatorName + "\");\n");
        codeBlockBuilder.add("  this." + activatorReferenceVar + " = (" + activatorName + ")" + activatorClassVar + ".newInstance();");
        codeBlockBuilder.add("\n");

        return new FieldAndConstructorInstantiation(
                fieldSpec,
                codeBlockBuilder.build()
        );
    }

    public MethodSpec generateTraverse(List<Activator> orderOfExecution, FlowName flowName, Connector input) {
        ClassName parameterClassType = input.type();
        String first;
        if (orderOfExecution.size() == 0) {
            first = "d";
        } else {
            first = connectorToVariable.get(input);
        }

        ParameterSpec dataParameterSpec = ParameterSpec.builder(parameterClassType, first).build();

        MethodSpec.Builder methodSpecBuilder = MethodSpec
                .methodBuilder(flowName.value())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(dataParameterSpec);

        // Every processor except the first and last.
        for (int i = 1; i < orderOfExecution.size() - 1; i++) {
            Activator activator = orderOfExecution.get(i);

            if (activator instanceof ProcessActivator processActivator) {
                Flow flow = processActivator.getFlow(flowName);
                String connectorVar = connectorToVariable.get(processActivator.getOutput(flowName));
                String activatorReferenceVar = activatorToVariable.get(activator);
                String functionName = flow.functionName();

                StringBuilder processorCallSB = new StringBuilder();
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
                        processorCallSB.append("this.")
                                .append(activatorReferenceVar)
                                .append(".query_")
                                .append(queryConnector.database().name().value())
                                .append("_")
                                .append(functionName)
                                .append("(");

                        flow.inputs()
                                .stream()
                                .filter(dbConnectorInput -> !(dbConnectorInput instanceof QueryConnector))
                                .forEach(dbConnectorInput ->
                                        processorCallSB.append(connectorToVariable.get(dbConnectorInput))
                                                .append(","));

                        //Removes the last , from the previous forEach
                        processorCallSB.setLength(processorCallSB.length() - 1);
                        processorCallSB.append(")")
                                .append(".createQuery(this.")
                                .append(activatorToVariable.get(queryConnector.database()))
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
            } else {
                throw new IllegalStateException("Must be ProcessActivator here");
            }
        }

        // Last activator can either be the same external entity as the start of a database store.
        Activator lastActivator = orderOfExecution.get(orderOfExecution.size() - 1);

        if (lastActivator instanceof DatabaseActivator databaseActivator) {
            StringBuilder sb = new StringBuilder();
            sb.append("this.")
                    .append(activatorToVariable.get(databaseActivator))
                    .append(".")
                    .append(flowName.value())
                    .append("(")
                    .append(connectorToVariable.get(databaseActivator.stores().get(flowName)))
                    .append(");\n");

            methodSpecBuilder.addCode(CodeBlock.of(sb.toString()));
        } else if (lastActivator instanceof ExternalEntityActivator externalEntityActivator) {
            Activator firstActivator = orderOfExecution.get(0);
            if (firstActivator != lastActivator) {
                throw new IllegalStateException("If flow ends with external entity, then it must begin with the same one");
            }

            Connector lastConnector = externalEntityActivator.end(flowName)
                    .orElseThrow(IllegalStateException::new);

            String returnVariable = connectorToVariable.get(lastConnector);

            CodeBlock returnStatement = CodeBlock.builder().add("return " + returnVariable + ";").build();
            methodSpecBuilder.addCode(returnStatement);
            ClassName returnClassType = lastConnector.type();
            methodSpecBuilder.returns(returnClassType);
        }

        return methodSpecBuilder.build();
    }

}
