package holt;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import holt.activator.ActivatorAggregate;
import holt.activator.Connector;
import holt.activator.FlowThroughAggregate;
import holt.activator.ProcessActivatorAggregate;
import holt.activator.QualifiedName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static holt.JavaFileGenerator.PACKAGE_NAME;
import static holt.JavaFileGenerator.toTypeName;

public final class PrivacyActivatorJavaFileGenerator {

    private PrivacyActivatorJavaFileGenerator() {}

    /**
     * Generates and saves two files.
     * First is a class that combines all the input connectors to one record
     * Second one is the combine process that extends the given requirements and combines the two.
     */
    public static void generateCombine(ProcessActivatorAggregate processActivatorAggregate, ProcessingEnvironment env, String dfdPackageName) {
        // Must only be one flow
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
                .build();

        // Modify the activator aggregate so the requirements file is generated correctly
        flow.setOutputType(new QualifiedName(dfdPackageName + "." + processActivatorAggregate.name().value() +  ".Combo"), false);

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
                .build();

        JavaFile combineActivatorJavaFile = JavaFile.builder(dfdPackageName, combineActivatorTypeSpec).build();
        try {
            combineActivatorJavaFile.writeTo(env.getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }



}
