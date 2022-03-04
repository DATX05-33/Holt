package holt.processor.generation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import holt.processor.OldNode;
import holt.processor.NodeType;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
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

    private final Map<String, TypeMirror> nameToTypeMirrorMap = new HashMap<>();

    public String getPACKAGE_NAME() {
        return PACKAGE_NAME;
    }

    private final String PACKAGE_NAME = "holt.processor.generation.interfaces";

    private final Map<String, OldNode> nodeMap = new HashMap<>();

    private static CodeGenerator instance = null;

    public static CodeGenerator getInstance() {
        if (instance == null) {
            instance = new CodeGenerator();
            return instance;
        } else {
            return instance;
        }
    }

    private CodeGenerator() {
    }

    public void setNodes(List<OldNode> nodes) {
        for (OldNode n : nodes) {
            nodeMap.put(n.name(), n);
        }
    }

    public void addTypeMirror(String name, TypeMirror typeMirror) {
        nameToTypeMirrorMap.put(name, typeMirror);
    }

    public void addOutputTypeAndFunctionName(TypeMirror source, TypeMirror outputType, TypeMirror target, String functionName) {
        outputTypes.put(source, outputType);
        functionNames.put(source, functionName);

        // add the from's output as the input for the to
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

    public List<JavaFile> generateInterfaces() {
        List<JavaFile> interfaces = new ArrayList<>();

        for (TypeMirror currentProcess : outputTypes.keySet()) {
            String currentSimpleName = simpleName(currentProcess);

            MethodSpec.Builder methodSpecBuilder = MethodSpec
                    .methodBuilder(functionNames.get(currentProcess))
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

            if (inputTypes.get(currentProcess) != null) {
                for (int i = 0; i < inputTypes.get(currentProcess).size(); i++) {
                    ClassName parameterClassName = ClassName.bestGuess(inputTypes.get(currentProcess).get(i).toString());
                    methodSpecBuilder.addParameter(
                            parameterClassName,
                            "input" + i
                    );
                }
            }

            TypeSpec.Builder anInterfaceBuilder = TypeSpec
                    .interfaceBuilder("I" + currentSimpleName)
                    .addModifiers(Modifier.PUBLIC);

            // add database connection
            if (nodeMap.get(currentSimpleName).nodeType().equals(NodeType.PROCESS)) {
                List<TypeMirror> dbTypes = findDatabaseInput(currentProcess);

                for (int i = 0; i < dbTypes.size(); i++) {
                    methodSpecBuilder.addParameter(
                            Object.class,
                            "input" + (i+1)
                    );
                }

                // for each database that's connected to this process
                for (TypeMirror db : dbTypes) {
                    JavaFile DBQuery = generateDBQueryInterface(db, currentProcess);
                    interfaces.add(DBQuery);

                    ClassName returnClass = ClassName.bestGuess( PACKAGE_NAME + "." + DBQuery.typeSpec.name);
                    ClassName parameterClass = ClassName.bestGuess(inputTypes.get(currentProcess).get(0).toString());
                    // TODO: parameterClass only works right now if currentProcess only has one input
                    MethodSpec methodSpec = MethodSpec
                            .methodBuilder("get" + simpleName(db) + "Query")
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .addParameter(parameterClass, "input")
                            .returns(returnClass)
                            .build();

                    anInterfaceBuilder.addMethod(methodSpec);
                }
            }

            ClassName returnClassName = ClassName.bestGuess(outputTypes.get(currentProcess).toString());
            methodSpecBuilder.returns(returnClassName);

            MethodSpec methodSpec = methodSpecBuilder.build();

            anInterfaceBuilder.addMethod(methodSpec);

            JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, anInterfaceBuilder.build())
                    .build();

            interfaces.add(javaFile);
        }

        return interfaces;
    }

    private List<TypeMirror> findDatabaseInput(TypeMirror process) {
        List<TypeMirror> databases = new ArrayList<>();
        OldNode node = nodeMap.get(simpleName(process));

        throw new UnsupportedOperationException();

//        for (OldNode a : node.inputs()) {
//            if (a.nodeType().equals(NodeType.LIMIT)) {
//                for (OldNode b : a.inputs()) {
//                    if (b.nodeType().equals(NodeType.DATA_BASE)) {
//                        databases.add(nameToTypeMirrorMap.get(b.name()));
//                    }
//                }
//            }
//        }

//        return databases;
    }

    private String simpleName(TypeMirror typeMirror) {
        return typeMirror.toString().substring(typeMirror.toString().lastIndexOf('.') + 1);
    }

    private JavaFile generateDBQueryInterface(TypeMirror dbType, TypeMirror processor) {
        ClassName paramClassName = ClassName.bestGuess(nameToTypeMirrorMap.get(simpleName(dbType)).toString());

        MethodSpec methodSpec = MethodSpec
                .methodBuilder("query")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(paramClassName, "db")
                .returns(Object.class) // This will change later with the Query annotation
                .build();

        String interfaceName = dbType.toString().substring(dbType.toString().lastIndexOf('.') + 1) +
                "To" +
                processor.toString().substring(processor.toString().lastIndexOf('.') + 1) +
                "Query";

        TypeSpec anInterface = TypeSpec
                .interfaceBuilder("I" + interfaceName)
                .addMethod(methodSpec)
                .addModifiers(Modifier.PUBLIC)
                .build();

        return JavaFile.builder(PACKAGE_NAME, anInterface)
                .build();
    }

    private void print(JavaFile javaFile) {
        try {
            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TypeMirror findTarget(TypeElement element) {
        OldNode n = nodeMap.get(element.getSimpleName().toString());

        // TODO: Is stepping two steps forward a good thing?
        //  It's mentioned that the developers are supposed to be able to edit the PADFD. What happens then?

        List<OldNode> outputs1 = n.outputs();

        for (OldNode a : outputs1) {
            // all a should be limits or requests
            // TODO: Second loop is only relevant for Limits. Maybe add if statement
            List<OldNode> outputs2 = a.outputs();
            for (OldNode b : outputs2) {
                TypeMirror outputTargetType = nameToTypeMirrorMap.get(b.name());
                // if it is null, that means that it's a log, limit, reason, etc
                if (outputTargetType != null) {
                    return outputTargetType;
                }
            }
        }

        return null;
    }
}
