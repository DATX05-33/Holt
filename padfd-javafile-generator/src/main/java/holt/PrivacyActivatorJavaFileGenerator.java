package holt;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import holt.activator.ActivatorAggregate;
import holt.activator.Connector;
import holt.activator.DatabaseActivatorAggregate;
import holt.activator.Domain;
import holt.activator.ExternalEntityActivatorAggregate;
import holt.activator.FlowOutput;
import holt.activator.FlowThroughAggregate;
import holt.activator.ProcessActivatorAggregate;
import holt.activator.QualifiedName;
import holt.activator.QueryInput;
import holt.activator.TraverseName;
import holt.padfd.metadata.CombineMetadata;
import holt.padfd.metadata.QuerierMetadata;

import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class PrivacyActivatorJavaFileGenerator {

    private PrivacyActivatorJavaFileGenerator() {}

    public static List<JavaFile> convertToJavaFiles(Domain domain, ProcessingEnvironment processingEnvironment) {
        String dfdPackageName = packageOf(domain);
        List<JavaFile> files = new ArrayList<>();

        for (ActivatorAggregate activator : domain.activators()) {
            if (activator instanceof ProcessActivatorAggregate processActivatorAggregate) {
                processActivatorAggregate.flows();
                if (processActivatorAggregate.metadata() instanceof CombineMetadata) {
                    files.addAll(generateCombine(processActivatorAggregate, processingEnvironment, dfdPackageName));
                } else if (processActivatorAggregate.metadata() instanceof QuerierMetadata querierMetadata) {
                    Map.Entry<TraverseName, FlowThroughAggregate> flowThroughEntry = processActivatorAggregate.flowsMap().entrySet().stream().findFirst().orElseThrow();
                    TraverseName traverseName = flowThroughEntry.getKey();
                    FlowThroughAggregate flow = flowThroughEntry.getValue();

                    QueryInput queryInput = flow.queries().get(0);
                    flow.setOutputType(queryInput.queryInputDefinition().output().type(), queryInput.queryInputDefinition().output().isCollection());

                    generateQuerier(processActivatorAggregate, processingEnvironment, dfdPackageName);
                }
            }
        }

        return files;
    }

    /**
     * Generates and saves two files.
     * First is a class that combines all the input connectors to one record
     * Second one is the combine process that extends the given requirements and combines the two.
     */
    private static List<JavaFile> generateCombine(ProcessActivatorAggregate processActivatorAggregate, ProcessingEnvironment env, String dfdPackageName) {
        if (processActivatorAggregate.flows().size() != 1) {
            throw new IllegalStateException("Can only be one flow for a Combine process");
        }

        FlowThroughAggregate flow = processActivatorAggregate.flows().get(0);

        // Class that combines stuff.
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        List<CodeBlock> codeBlocks = new ArrayList<>();
        for (int i = 0; i < flow.inputs().size(); i++) {
            Connector connector = flow.inputs().get(i);
            TypeName typeName = toTypeName(connector);
            String varName = "v" + i;
            fieldSpecs.add(
                    FieldSpec
                            .builder(typeName, varName)
                            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                            .build()
            );
            parameterSpecs.add(
                    ParameterSpec
                            .builder(typeName, varName)
                            .build()
            );
            codeBlocks.add(CodeBlock.of("this." + varName + " = " + varName + ";"));
        }

        MethodSpec comboConstructor = MethodSpec
                .constructorBuilder()
                .addParameters(parameterSpecs)
                .addCode(CodeBlock.join(codeBlocks, "\n"))
                .build();

        TypeSpec comboClass = TypeSpec
                .classBuilder("Combo")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addFields(fieldSpecs)
                .addMethod(comboConstructor)
                .addAnnotation(getGeneratedAnnotation())
                .build();

        // Modify the activator aggregate so the requirements file is generated correctly
        flow.setOutputType(QualifiedName.of(dfdPackageName + "." + processActivatorAggregate.name().value() +  ".Combo"), false);

        StringBuilder returnSB = new StringBuilder();
        returnSB.append("return new Combo(");

        List<ParameterSpec> parameters = new ArrayList<>();
        for (int i = 0; i < flow.inputs().size(); i++) {
            Connector connector = flow.inputs().get(i);
            String varName = "input" + i;
            parameters.add(
                    ParameterSpec
                            .builder(toTypeName(connector), varName)
                            .build()
            );
            returnSB
                    .append(varName)
                    .append(",");
        }

        //Removes the last , from the previous forEach, if there was any input to the query
        if (flow.inputs().size() > 0) {
            returnSB.setLength(returnSB.length() - 1);
        }

        returnSB.append(");");

        CodeBlock returnCodeBlock = CodeBlock.of(returnSB.toString());

        MethodSpec combineMethodSpec = MethodSpec
                .methodBuilder(flow.functionName().value())
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameters)
                .addAnnotation(Override.class)
                .returns(toTypeName(flow.output()))
                .addCode(returnCodeBlock)
                .build();

        TypeSpec combineActivatorTypeSpec = TypeSpec.classBuilder(processActivatorAggregate.name().value())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.bestGuess(dfdPackageName + "." + processActivatorAggregate.requirementsName().value()))
                .addMethod(combineMethodSpec)
                .addType(comboClass)
                .addAnnotation(getGeneratedAnnotation())
                .build();

        return Collections.singletonList(JavaFile.builder(dfdPackageName, combineActivatorTypeSpec).build());
    }

    private static List<JavaFile> generateQuerier(ProcessActivatorAggregate processActivatorAggregate, ProcessingEnvironment env, String dfdPackageName) {
        if (processActivatorAggregate.flows().size() != 1) {
            throw new IllegalStateException("Can only be one flow for a Combine process");
        }

        FlowThroughAggregate flow = processActivatorAggregate.flows().get(0);
//
//        if (flow.inputs().size() != 1) {
//            throw new IllegalStateException("Can only be one input for the querier flow");
//        }

        return Collections.emptyList();
    }

    private static List<JavaFile> generateCombiner(ProcessActivatorAggregate processActivatorAggregate, ProcessingEnvironment env, String dfdPackageName) {
        return Collections.emptyList();
    }

    private static AnnotationSpec getGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("\"" + PrivacyActivatorJavaFileGenerator.class.getName() + "\""))
                .build();
    }

    public static String packageOf(Domain domain) {
        return PACKAGE_NAME + "." + domain.name().value();
    }

    public static final String PACKAGE_NAME = "holt.processor.generation";

    public static TypeName toTypeName(QualifiedName qualifiedName) {
        if (qualifiedName.types() == null) {
            return ClassName.bestGuess(qualifiedName.value());
        } else{
            TypeName[] types = new TypeName[qualifiedName.types().length];
            QualifiedName[] qualifiedNames = qualifiedName.types();
            for (int i = 0; i < qualifiedNames.length; i++) {
                QualifiedName type = qualifiedNames[i];
                types[i] = ClassName.bestGuess(type.value());
            }
            return ParameterizedTypeName.get(
                    ClassName.bestGuess(qualifiedName.value()),
                    types
            );
        }
    }

    public static TypeName toTypeName(FlowOutput flowOutput) {
        TypeName connectorTypeName = toTypeName(flowOutput.type());
        if (flowOutput.isCollection()) {
            ClassName collection = ClassName.get(Collection.class);
            return ParameterizedTypeName.get(collection, connectorTypeName);
        } else {
            return connectorTypeName;
        }
    }

    public static TypeName toTypeName(Connector connector) {
        return toTypeName(connector.flowOutput());
    }

    public static TypeName toTypeName(String qualifiedName) {
        return ClassName.bestGuess(qualifiedName);
    }

}
