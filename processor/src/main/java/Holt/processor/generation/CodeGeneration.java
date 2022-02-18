package Holt.processor.generation;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Map;

public class CodeGeneration {

    private static final String PACKAGE_NAME = "Holt.processor.generation.interfaces";

    static JavaFile generateInterface(String name) {
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

    public static JavaFile generateProcessExt(String name, String extensionName) {
        ParameterizedTypeName map = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(Object.class));

        MethodSpec process = MethodSpec.methodBuilder("process")
                .addModifiers(Modifier.PUBLIC)
                .returns(map)
                .addParameter(map, "input")
                .addStatement("$T.out.println($L)", System.class, "input")
                .addStatement("return $L", "input")
                .build();

        TypeName extensionClass = ClassName.get(PACKAGE_NAME, extensionName);

        TypeSpec classGen = TypeSpec.classBuilder(name)
                .superclass(extensionClass)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(process)
                .build();

        return JavaFile.builder("Holt.codeGeneration.custom", classGen).build();
    }

    public static JavaFile generateProcessImpl(String name, String interfaceName) {
        ParameterizedTypeName map = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(Object.class));

        MethodSpec process = MethodSpec.methodBuilder("process")
                .addModifiers(Modifier.PUBLIC)
                .returns(map)
                .addParameter(map, "input")
                .addStatement("$T.out.println($L)", System.class, "input")
                .addStatement("return $L", "input")
                .build();

        TypeName interfaceTypeName = ClassName.get(PACKAGE_NAME, interfaceName);

        TypeSpec classGen = TypeSpec.classBuilder(name)
                .addSuperinterface(interfaceTypeName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(process)
                .build();

        return JavaFile.builder("Holt.codeGeneration.custom", classGen).build();
    }

    public static JavaFile generateCustomProcess(String name, String interfaceName) {
        JavaFile javaFile = generateProcessImpl(name, interfaceName);
        saveAndPrint(javaFile);
        return javaFile;
    }

    public static JavaFile generateExternalEntity(String name, String interfaceName) {
        JavaFile javaFile = generateProcessExt(name, interfaceName);
        saveAndPrint(javaFile);
        return javaFile;
    }

    public static JavaFile generateReasonProcess(String name, String interfaceName) {
        JavaFile javaFile = generateProcessExt(name, interfaceName);
        saveAndPrint(javaFile);
        return javaFile;
    }

    public static JavaFile generateRequestProcess(String name, String interfaceName) {
        JavaFile javaFile = generateProcessExt(name, interfaceName);
        saveAndPrint(javaFile);
        return javaFile;
    }

    public static JavaFile generateLimitProcess(String name, String interfaceName) {
        JavaFile javaFile = generateProcessExt(name, interfaceName);
        saveAndPrint(javaFile);
        return javaFile;
    }

    public static JavaFile generateLogProcess(String name, String interfaceName) {
        JavaFile javaFile = generateProcessExt(name, interfaceName);
        saveAndPrint(javaFile);
        return javaFile;
    }

    public static JavaFile generateLogDBProcess(String name, String interfaceName) {
        JavaFile javaFile = generateProcessExt(name, interfaceName);
        saveAndPrint(javaFile);
        return javaFile;
    }


    static void saveAndPrint(JavaFile javaFile) {
        try {
            // print
            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
