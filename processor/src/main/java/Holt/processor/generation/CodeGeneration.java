package Holt.processor.generation;

import com.squareup.javapoet.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Map;

public class CodeGeneration {

    private static final String PACKAGE_NAME = "Holt.processor.generation.interfaces";

    public static JavaFile generateInterface(String name) {
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

    public static JavaFile generateMethodFromAnnotation(Element element, Class input, Class output, String methodName) {
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

    static void print(JavaFile javaFile) {
        try {
            // print
            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
