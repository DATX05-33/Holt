package holt.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import holt.processor.activator.Activator;
import holt.processor.activator.Activators;
import holt.processor.activator.Connector;
import holt.processor.activator.ExternalEntityActivator;
import holt.processor.activator.FlowName;

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


        // All the activators that need instantiation
        List<Activator> activatorsToInstansiate = activators.flows().values()
                .stream()
                .flatMap(Collection::stream)
                .distinct()
                .filter(activator -> !(activator instanceof ExternalEntityActivator))
                .toList();

        int connectorVariableIndex = 0;
        for (Activator activator : activatorsToInstansiate) {
            var activatorCode = generateCodeForActivator(activator);
            fieldSpecs.add(activatorCode.fieldSpec);
            constructorSpecBuilder.addCode(activatorCode.instantiation);



            connectorVariableIndex++;
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
        String activatorReferenceVar = activatorName + "Ref";

        ClassName activatorClassName = ClassName.bestGuess("holt.test.friend." + activatorName);
        FieldSpec fieldSpec = FieldSpec.builder(
                activatorClassName,
                activatorReferenceVar,
                Modifier.PRIVATE,
                Modifier.FINAL
        ).build();

        String activatorClassVar = activatorName + "_";
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        codeBlockBuilder.add("  Class " + activatorClassVar + " = Class.forName(\"" + activatorName + "\");\n");
        codeBlockBuilder.add("  this." + activatorReferenceVar + " = (" + activatorName + ")" + activatorClassVar + ".newInstance();");
        codeBlockBuilder.add("\n");

        return new FieldAndConstructorInstantiation(
                fieldSpec,
                codeBlockBuilder.build()
        );
    }

    public MethodSpec generateTraverse(List<Activator> orderOfExecution, FlowName flowName, Connector input, Connector output) {
        ClassName parameterClassType = input.type();
        ParameterSpec dataParameterSpec = ParameterSpec.builder(parameterClassType, "d").build();
        ParameterSpec policyParameterSpec = ParameterSpec.builder(Object.class, "pol").build();

        MethodSpec.Builder methodSpecBuilder = MethodSpec
                .methodBuilder(flowName.value())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(dataParameterSpec)
                .addParameter(policyParameterSpec);

        if (output != null) {
            CodeBlock returnStatement = CodeBlock.builder().add("return null;").build();
            methodSpecBuilder.addCode(returnStatement);
            ClassName returnClassType = output.type();
            methodSpecBuilder.returns(returnClassType);
        }

        return methodSpecBuilder.build();
    }

}
