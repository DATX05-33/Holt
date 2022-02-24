package holt.processor.generation;

import com.squareup.javapoet.*;
import holt.processor.Node;
import holt.processor.NodeType;

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

    private List<Node> nodes;
    private Map<String, Node> nodeMap;

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

    public void setNodes(List<Node> nodes) {
        for (Node n : nodes) {
            nodeMap.put(n.name(), n);
        }
        this.nodes = nodes;
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

    public List<JavaFile> generateInterfaces() {
        List<JavaFile> interfaces = new ArrayList<>();
        for (TypeMirror name : outputTypes.keySet()) {
            String currentSimpleName = fullyQualifiedNameToSimpleName(name);

            MethodSpec.Builder methodSpecBuilder = MethodSpec
                    .methodBuilder(functionNames.get(name))
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

            // add inputs
            if (inputTypes.get(name) != null) {
                for (int i = 0; i < inputTypes.get(name).size(); i++) {
                    ClassName parameterClassName = ClassName.bestGuess(inputTypes.get(name).get(i).toString());
                    methodSpecBuilder.addParameter(
                            parameterClassName,
                            "input" + i
                    );
                }
            }

            // add database connection
            if (nodeMap.get(currentSimpleName).nodeType().equals(NodeType.CUSTOM_PROCESS)) {
                // TODO: How do you find this? The TypeMirror/ClassName for the Database that's connected to this processor
                List<TypeMirror> dbTypes = findDBNodes(name);

                for (TypeMirror t : dbTypes) {
                    JavaFile DBQuery = generateDBQueryInterface(t, name /*for example FormatFriend*/);
                    // TODO: Add the query methodSpec
                }
            }

            ClassName returnClassName = ClassName.bestGuess(outputTypes.get(name).toString());
            methodSpecBuilder.returns(returnClassName);

            MethodSpec methodSpec = methodSpecBuilder.build();

            TypeSpec anInterface = TypeSpec
                    .interfaceBuilder("I" + currentSimpleName)
                    .addMethod(methodSpec)
                    .addModifiers(Modifier.PUBLIC)
                    .build();

            JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, anInterface)
                    .build();

            System.out.println("----------------------------");
            print(javaFile);
            System.out.println("----------------------------");
            interfaces.add(javaFile);
        }

        return interfaces;
    }

    public List<TypeMirror> findInputNodesWithType(TypeMirror process, NodeType nodeType) {
        List<TypeMirror> nodes = new ArrayList<>();

        if (inputTypes.get(process) == null) {
            throw new NullPointerException("No specified input type for " + process.toString());
        }

        for (TypeMirror l : inputTypes.get(process)) {
            // l should all be Limit
            String lName = fullyQualifiedNameToSimpleName(l);
            System.out.println("Limit: " + lName);

            for (TypeMirror t : inputTypes.get(l)) {
                // t can be a database
                String tName = fullyQualifiedNameToSimpleName(l);
                if (nodeMap.get(tName).nodeType().equals(nodeType)) {
                    // t is nodeType
                    nodes.add(t);
                }
            }
        }

        return nodes;
    }

    private String fullyQualifiedNameToSimpleName(TypeMirror typeMirror) {
        return typeMirror.toString().substring(typeMirror.toString().lastIndexOf('.') + 1);
    }

    private JavaFile generateDBQueryInterface(TypeMirror dbType, TypeMirror processor) {
        ClassName returnClassName = ClassName.bestGuess(outputTypes.get(dbType).toString());

        MethodSpec methodSpec = MethodSpec
                .methodBuilder("query")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(returnClassName, "db")
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

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, anInterface)
                .build();

        return javaFile;
    }

    private void print(JavaFile javaFile) {
        try {
            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
