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
import holt.activator.ConnectedClass;
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
import holt.padfd.metadata.GuardMetadata;
import holt.padfd.metadata.LogMetadata;
import holt.padfd.metadata.QuerierMetadata;

import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                } else if (processActivatorAggregate.metadata() instanceof QuerierMetadata) {
                    Map.Entry<TraverseName, FlowThroughAggregate> flowThroughEntry = processActivatorAggregate.flowsMap().entrySet().stream().findFirst().orElseThrow();
                    FlowThroughAggregate flow = flowThroughEntry.getValue();

                    QueryInput queryInput = flow.queries().get(0);
                    flow.setOutputType(queryInput.queryInputDefinition().output().type(), queryInput.queryInputDefinition().output().isCollection());

                    files.addAll(generateQuerier(processActivatorAggregate, processingEnvironment, dfdPackageName));
                } else if (processActivatorAggregate.metadata() instanceof GuardMetadata) {
                    files.addAll(generateGuard(processActivatorAggregate, processingEnvironment, dfdPackageName));
                } else if (processActivatorAggregate.metadata() instanceof LogMetadata) {
                    files.addAll(generateLog(processActivatorAggregate, processingEnvironment, dfdPackageName));
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
            System.err.println("Can only be one flow for a Combine process");
            return new ArrayList<>();
        }

        FlowThroughAggregate flow = processActivatorAggregate.flows().get(0);

        TypeSpec comboTypeSpec = createCombo("Combo", flow.inputs().stream().map(Connector::flowOutput).toList(), null);

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
                .addType(comboTypeSpec)
                .addAnnotation(getGeneratedAnnotation())
                .build();

        processActivatorAggregate.setConnectedClass(new ConnectedClass(QualifiedName.of(dfdPackageName + "." + processActivatorAggregate.name().value()), true));
        return Collections.singletonList(JavaFile.builder(dfdPackageName, combineActivatorTypeSpec).build());
    }

    private static List<JavaFile> generateQuerier(ProcessActivatorAggregate processActivatorAggregate, ProcessingEnvironment env, String dfdPackageName) {
        if (processActivatorAggregate.flows().size() != 1) {
            System.err.println("Can only be one flow for a Combine process");
            return new ArrayList<>();
        }

        FlowThroughAggregate flow = processActivatorAggregate.flows().get(0);

        if (flow.queries().size() != 1 && flow.inputs().size() == 0) {
            throw new IllegalStateException("Can only be one query input for the querier flow");
        }

        QueryInput queryInput = flow.queries().get(0);
        TypeName queryInputTypeName = toTypeName(queryInput.queryInputDefinition().output());

        ParameterSpec queryInputParameterSpec = ParameterSpec
                .builder(queryInputTypeName, "queryResult")
                .build();

        MethodSpec querierMethodSpec = MethodSpec
                .methodBuilder(flow.functionName().value())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(queryInputParameterSpec)
                .addCode(CodeBlock.of("return queryResult;"))
                .returns(queryInputTypeName)
                .build();

        TypeSpec querierTypeSpec = TypeSpec.classBuilder(processActivatorAggregate.name().value())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.bestGuess(dfdPackageName + "." + processActivatorAggregate.requirementsName().value()))
                .addMethod(querierMethodSpec)
                .addAnnotation(getGeneratedAnnotation())
                .build();

        processActivatorAggregate.setConnectedClass(new ConnectedClass(QualifiedName.of(dfdPackageName + "." + processActivatorAggregate.name().value()), true));
        return Collections.singletonList(JavaFile.builder(dfdPackageName, querierTypeSpec).build());
    }

    private static List<JavaFile> generateGuard(ProcessActivatorAggregate processActivatorAggregate, ProcessingEnvironment env, String dfdPackageName) {
        if (processActivatorAggregate.flows().size() != 1) {
            System.err.println("Can only be one flow for a Guard process");
            return new ArrayList<>();
        }

        FlowThroughAggregate flow = processActivatorAggregate.flows().get(0);

        if (flow.queries().size() == 0 && flow.inputs().size() != 2) {
            throw new IllegalStateException("Can only be two inputs for the guard flow");
        }

        Connector predicateConnector = flow.inputs().get(0);
        Connector dataConnector = flow.inputs().get(1);

        TypeName predicateTypeName = toTypeName(predicateConnector);
        TypeName dataTypeName = toTypeName(dataConnector);

        ParameterSpec predicateParameterSpec = ParameterSpec
                .builder(predicateTypeName, "tester")
                .build();

        ParameterSpec dataParameterSpec = ParameterSpec
                .builder(dataTypeName, "data")
                .build();

        StringBuilder methodSB = new StringBuilder();
        if (dataConnector.flowOutput().isCollection()) {
            methodSB.append("return data\n");
            methodSB.append("  .stream()\n");
            methodSB.append("  .filter(tester::test)\n");
            methodSB.append("  .toList();\n");
        } else {
            methodSB.append("if (tester.test(data)) {\n");
            methodSB.append("  return data;\n");
            methodSB.append("} else {\n");
            methodSB.append("  throw new IllegalStateException();\n");
            methodSB.append("}");
        }

        MethodSpec guardMethodSpec = MethodSpec
                .methodBuilder(flow.functionName().value())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addCode(methodSB.toString())
                .addParameters(List.of(predicateParameterSpec, dataParameterSpec))
                .returns(dataTypeName)
                .build();

        TypeSpec guardTypeSpec = TypeSpec.classBuilder(processActivatorAggregate.name().value())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.bestGuess(dfdPackageName + "." + processActivatorAggregate.requirementsName().value()))
                .addMethod(guardMethodSpec)
                .addAnnotation(getGeneratedAnnotation())
                .build();

        processActivatorAggregate.setConnectedClass(new ConnectedClass(QualifiedName.of(dfdPackageName + "." + processActivatorAggregate.name().value()), true));
        return Collections.singletonList(JavaFile.builder(dfdPackageName, guardTypeSpec).build());
    }

    private static List<JavaFile> generateLog(ProcessActivatorAggregate processActivatorAggregate, ProcessingEnvironment env, String dfdPackageName) {
        if (processActivatorAggregate.flows().size() != 1) {
            System.err.println("Can only be one flow for a Guard process");
            return new ArrayList<>();
        }

        FlowThroughAggregate flow = processActivatorAggregate.flows().get(0);
        if (flow.queries().size() == 0 && flow.inputs().size() != 3) {
            throw new IllegalStateException("Can only be three inputs for the log flow");
        }

        Connector predicateConnector = flow.inputs().get(0);
        Connector policyMapConnector = flow.inputs().get(1);
        Connector dataConnector = flow.inputs().get(2);

        TypeName predicateTypeName = toTypeName(predicateConnector);
        TypeName policyMapTypeName = toTypeName(policyMapConnector);
        TypeName dataTypeName = toTypeName(dataConnector);

        ParameterSpec predicateParameterSpec = ParameterSpec
                .builder(predicateTypeName, "tester")
                .build();

        ParameterSpec policyMapParameterSpec = ParameterSpec
                .builder(policyMapTypeName, "policyMap")
                .build();

        ParameterSpec dataParameterSpec = ParameterSpec
                .builder(dataTypeName, "data")
                .build();

        String comboName = "Row";
        TypeSpec rowTypeSpec = createCombo(
                comboName,
                List.of(
                        new FlowOutput(dataConnector.flowOutput().type(), false),
                        new FlowOutput(policyMapConnector.flowOutput().type().types().get(1), false),
                        new FlowOutput(QualifiedName.of("java.lang.Boolean"), false),
                        new FlowOutput(QualifiedName.of("java.time.Instant"), false)
                ),
                List.of("data", "policy", "result", "time")
        );

        flow.setOutputType(QualifiedName.of(dfdPackageName + "." + processActivatorAggregate.name().value() + "." + comboName), dataConnector.flowOutput().isCollection());

        StringBuilder logSB = new StringBuilder();
        if (dataConnector.flowOutput().isCollection()) {
            logSB.append("return data\n");
            logSB.append("  .stream()\n");
            logSB.append("   .map(d -> new Row(d, policyMap.get(d), tester.test(d), Instant.now()))\n");
            logSB.append("  .toList();\n");
        } else {
            logSB.append("return new Row(data, policyMap.get(data), tester.test(data), Instant.now());");
        }

        MethodSpec logMethodSpec = MethodSpec
                .methodBuilder(flow.functionName().value())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameters(List.of(predicateParameterSpec, policyMapParameterSpec, dataParameterSpec))
                .returns(toTypeName(flow.output()))
                .addCode(logSB.toString())
                .build();

        TypeSpec logTypeSpec = TypeSpec.classBuilder(processActivatorAggregate.name().value())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.bestGuess(dfdPackageName + "." + processActivatorAggregate.requirementsName().value()))
                .addMethod(logMethodSpec)
                .addAnnotation(getGeneratedAnnotation())
                .addType(rowTypeSpec)
                .build();

        processActivatorAggregate.setConnectedClass(new ConnectedClass(QualifiedName.of(dfdPackageName + "." + processActivatorAggregate.name().value()), true));
        return Collections.singletonList(JavaFile.builder(dfdPackageName, logTypeSpec).build());
    }

    private static TypeSpec createCombo(String name, List<FlowOutput> inputs, List<String> varNames) {
        if (varNames == null) {
            varNames = new ArrayList<>();
            for (int i = 0; i < inputs.size(); i++) {
                varNames.add("v" + i);
            }
        }

        // Class that combines stuff.
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        List<CodeBlock> codeBlocks = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            FlowOutput flowOutput = inputs.get(i);
            TypeName typeName = toTypeName(flowOutput);
            String varName = varNames.get(i);
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

        StringBuilder toStringSB = new StringBuilder();

        toStringSB.append("return ");
        for (int i = 0; i < inputs.size(); i++) {
            String varName = varNames.get(i);
            toStringSB.append("\"" + varName + ": \" + " + varName + " + \", \" + ");
        }
        toStringSB.setLength(toStringSB.length() - 10);
        toStringSB.append(";");

        MethodSpec toStringMethodSpec = MethodSpec
                .methodBuilder("toString")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(String.class))
                .addCode(toStringSB.toString())
                .addAnnotation(Override.class)
                .build();

        MethodSpec comboConstructor = MethodSpec
                .constructorBuilder()
                .addParameters(parameterSpecs)
                .addCode(CodeBlock.join(codeBlocks, "\n"))
                .build();

        return TypeSpec
                .classBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addFields(fieldSpecs)
                .addMethods(List.of(comboConstructor, toStringMethodSpec))
                .addAnnotation(getGeneratedAnnotation())
                .build();
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
            TypeName[] types = new TypeName[qualifiedName.types().size()];
            List<QualifiedName> qualifiedNames = qualifiedName.types();
            for (int i = 0; i < qualifiedNames.size(); i++) {
                QualifiedName type = qualifiedNames.get(i);
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
