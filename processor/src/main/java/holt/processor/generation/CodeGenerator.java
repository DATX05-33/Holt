package holt.processor.generation;

import com.squareup.javapoet.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator {

    private final Map<TypeMirror, List<TypeMirror>> inputTypes = new HashMap<>();
    private final Map<TypeMirror, TypeMirror> outputTypes = new HashMap<>();
    private final Map<TypeMirror, String> functionNames = new HashMap<>();

    private final String PACKAGE_NAME = "holt.processor.generation.interfaces";

    public static void main(String[] args) {
        new CodeGenerator(1);
    }

    public CodeGenerator(int i) {
        /*addOutputTypeAndFunctionName(Test1.class, String.class, Test3.class,"one");
        addOutputTypeAndFunctionName(Test2.class, Integer.class, Test3.class, "two");
        addOutputTypeAndFunctionName(Test3.class, String.class, Test1.class, "three");


        generateInterfaces();*/
    }

    public CodeGenerator() {

    }

    public void addOutputTypeAndFunctionName(TypeMirror source, TypeMirror outputType, TypeMirror target, String functionName) {
        outputTypes.put(source, outputType);
        functionNames.put(source, functionName);

        // add the source's output as the input for the target
        addInputTypes(outputType, target);
    }

    public void addInputTypes(TypeMirror inputType, TypeMirror source) {
        if (inputTypes.get(source) == null || inputTypes.get(source).isEmpty()) {
            List<TypeMirror> TargetsInputs = new ArrayList<>();
            TargetsInputs.add(inputType);
            inputTypes.put(source, TargetsInputs);
        } else {
            inputTypes.get(source).add(inputType);
        }
    }

    public List<JavaFile> generateInterfaces() throws ClassNotFoundException {
        List<JavaFile> interfaces = new ArrayList<>();
        for (TypeMirror name : outputTypes.keySet()) {

            MethodSpec.Builder methodSpecBuilder = MethodSpec
                    .methodBuilder(functionNames.get(name))
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

            // add inputs
            if (inputTypes.get(name) != null) {
                for (int i = 0; i < inputTypes.get(name).size(); i++) {
                    ClassName parameterClassName = ClassName.bestGuess(inputTypes.get(name).get(i).toString());
                    methodSpecBuilder.addParameter(
                            //Class.forName(inputTypes.get(name).get(i).toString()),
                            parameterClassName,
                            "input" + i
                    );
                }
            }

            ClassName returnClassName = ClassName.bestGuess(outputTypes.get(name).toString());

            methodSpecBuilder.returns(returnClassName);
            MethodSpec methodSpec = methodSpecBuilder.build();

            TypeSpec anInterface = TypeSpec
                    .interfaceBuilder(name.toString().substring(name.toString().lastIndexOf('.') + 1))
                    .addMethod(methodSpec)
                    .addModifiers(Modifier.PUBLIC)
                    .build();

            JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, anInterface)
                    .build();

            System.out.println("----------------------------");
            print(javaFile);
            System.out.println("----------------------------");
            // TODO: Save javafile
            interfaces.add(javaFile);
        }

        return interfaces;
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
