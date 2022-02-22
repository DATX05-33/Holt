package Holt.processor.generation;

import com.squareup.javapoet.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator {

    private final Map<Class<?>, List<Class<?>>> inputTypes = new HashMap<>();
    private final Map<Class<?>, Class<?>> outputTypes = new HashMap<>();
    private final Map<Class<?>, String> functionNames = new HashMap<>();

    private final String PACKAGE_NAME = "Holt.processor.generation.interfaces";

    public void addOutputTypeAndFunctionName(Class<?> source, Class<?> outputType, Class<?> target, String functionName) {
        outputTypes.put(source, outputType);
        functionNames.put(source, functionName);

        // add the source's output as the input for the target
        addInputTypes(outputType, target);
    }

    public void addInputTypes(Class<?> inputType, Class<?> source) {
        if (inputTypes.get(source) == null || inputTypes.get(source).isEmpty()) {
            List<Class<?>> TargetsInputs = new ArrayList<>();
            TargetsInputs.add(inputType);
            inputTypes.put(source, TargetsInputs);
        } else {
            inputTypes.get(source).add(inputType);
        }
    }

    public void generateInterfaces() {
        for (Class<?> name : inputTypes.keySet()) {

            MethodSpec.Builder methodSpecBuilder = MethodSpec
                    .methodBuilder(functionNames.get(name))
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

            // add inputs
            for (int i = 0; i < inputTypes.get(name).size(); i++) {
                methodSpecBuilder.addParameter(
                        inputTypes.get(name).get(i),
                        "input" + i
                );
            }

            methodSpecBuilder.returns(outputTypes.get(name));
            MethodSpec methodSpec = methodSpecBuilder.build();

            TypeSpec anInterface = TypeSpec
                    .interfaceBuilder("I" + name.getSimpleName())
                    .addMethod(methodSpec)
                    .addModifiers(Modifier.PUBLIC)
                    .build();

            JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, anInterface)
                    .build();

            System.out.println("----------------------------");
            print(javaFile);
            System.out.println("----------------------------");
            // TODO: Save javafile
        }
    }


    public JavaFile generateInterface(String name) {
        ParameterizedTypeName map = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(Object.class));

        ParameterSpec input = ParameterSpec.builder(map, "input")
                .build();

        MethodSpec process = MethodSpec.methodBuilder("process")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(map)
                .addParameter(map, "input")
                .build();

        TypeSpec interfaceGen = TypeSpec.interfaceBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(process)
                .build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, interfaceGen)
                .build();

        return javaFile;
    }

    public JavaFile generateMethodFromAnnotation(Element element, Class<?> input, Class<?> output, String methodName) {
        // TODO: What about multiple inputs?
        // Maybe have input be a List<Class> inputs, alternatively with names as well
        MethodSpec method = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(output)
                .addParameter(input, "input")
                .build();

        TypeSpec interfaceGen = TypeSpec.interfaceBuilder(element.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .addMethod(method)
                .build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, interfaceGen)
                .build();

        print(javaFile);

        return javaFile;
    }

    public JavaFile createEnum(List<String> strings) {
        TypeSpec.Builder activatorEnum = TypeSpec.enumBuilder("Activator")
                .addModifiers(Modifier.PUBLIC);

        for (String s : strings) {
            activatorEnum.addEnumConstant(s.toUpperCase());
        }

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, activatorEnum.build())
                .build();

        print(javaFile);

        return javaFile;
    }

    private void print(JavaFile javaFile) {
        try {
            // print
            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
