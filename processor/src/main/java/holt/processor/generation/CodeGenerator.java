package holt.processor.generation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import holt.processor.Node;
import holt.processor.NodeType;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.*;

public class CodeGenerator {

    private final Map<TypeMirror, List<TypeMirror>> inputTypes = new HashMap<>();
    private final Map<TypeMirror, TypeMirror> outputTypes = new HashMap<>();
    private final Map<TypeMirror, String> functionNames = new HashMap<>();

    private final Map<String, TypeMirror> stringTypeMirrorMap = new HashMap<>();

    private final String PACKAGE_NAME = "holt.processor.generation.interfaces";

    private final Map<String, Node> nodeMap = new HashMap<>();

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
    }

    public void addTypeMirror(String name, TypeMirror typeMirror) {
        System.out.println("addTypeMirror: adding " + name + " -> " + typeMirror.toString());
        stringTypeMirrorMap.put(name, typeMirror);
    }

    public void addOutputTypeAndFunctionName(TypeMirror source, TypeMirror outputType, TypeMirror target, String functionName) {
        outputTypes.put(source, outputType);
        functionNames.put(source, functionName);

        // add the source's output as the input for the target
        addInputTypes(outputType, target);
    }

    public void addInputTypes(TypeMirror inputType, TypeMirror source) {
        System.out.println("Adding input types");
        if (inputTypes.get(source) == null || inputTypes.get(source).isEmpty()) {
            System.out.println("InputTypes was null or empty");
            System.out.println("Added " + inputType + " as input for " + source);
            List<TypeMirror> TargetsInputs = new ArrayList<>();
            TargetsInputs.add(inputType);
            inputTypes.put(source, TargetsInputs);
        } else {
            System.out.println("Added " + inputType + " as input for " + source);
            inputTypes.get(source).add(inputType);
        }
    }

    public List<JavaFile> generateInterfaces() {
        System.out.println("generateInterfaces :: start");
        List<JavaFile> interfaces = new ArrayList<>();

        for (TypeMirror name : outputTypes.keySet()) {
            String currentSimpleName = simpleName(name);
            System.out.println("generateInterfaces :: generating for " + currentSimpleName);

            MethodSpec.Builder methodSpecBuilder = MethodSpec
                    .methodBuilder(functionNames.get(name))
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

            System.out.println("generateInterfaces :: adding inputs");
            if (inputTypes.get(name) != null) {
                for (int i = 0; i < inputTypes.get(name).size(); i++) {
                    ClassName parameterClassName = ClassName.bestGuess(inputTypes.get(name).get(i).toString());
                    methodSpecBuilder.addParameter(
                            parameterClassName,
                            "input" + i
                    );
                }
            }
            System.out.println("generateInterfaces :: inputs added");

            TypeSpec.Builder anInterfaceBuilder = TypeSpec
                    .interfaceBuilder("I" + currentSimpleName)
                    .addModifiers(Modifier.PUBLIC);

            // add database connection
            if (nodeMap.get(currentSimpleName).nodeType().equals(NodeType.CUSTOM_PROCESS)) {
                List<TypeMirror> dbTypes = findInputNodesWithType(name, NodeType.DATA_BASE);


                System.out.println("DBTypes: " + Arrays.toString(dbTypes.toArray()));

                for (TypeMirror t : dbTypes) {
                    JavaFile DBQuery = generateDBQueryInterface(t /* for example FriendsDB */, name /*for example FormatFriend*/);
                    interfaces.add(DBQuery);

                    ClassName returnType = ClassName.bestGuess(t.toString());
                    MethodSpec methodSpec = MethodSpec
                            .methodBuilder("query")
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .addParameter(Object.class, "input")    // TODO: How do we find the parameter? Is it FriendsRaw? That's in the inputTypes thingy
                            .returns(returnType)
                            .build();

                    anInterfaceBuilder.addMethod(methodSpec);
                    System.out.println("generateInterfaces :: database added");
                }
            }

            System.out.println("generateInterfaces :: database connections done");

            ClassName returnClassName = ClassName.bestGuess(outputTypes.get(name).toString());
            methodSpecBuilder.returns(returnClassName);

            MethodSpec methodSpec = methodSpecBuilder.build();

            anInterfaceBuilder.addMethod(methodSpec);

            JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, anInterfaceBuilder.build())
                    .build();

            // System.out.println("----------------------------");
            // print(javaFile);
            // System.out.println("----------------------------");
            interfaces.add(javaFile);
        }

        System.out.println("generateInterfaces :: end");
        return interfaces;
    }

    public List<TypeMirror> findInputNodesWithType(TypeMirror process, NodeType nodeType) {
        List<TypeMirror> nodes = new ArrayList<>();

        System.out.println("findInputNodesWithType :: " + process.toString());

        if (inputTypes.get(process) == null) {
            throw new NullPointerException("No specified input type for " + process);
        }

        for (TypeMirror p : inputTypes.get(process)) {
            String pName = simpleName(p);
            System.out.println("findInputNodesWithType ::   loop " + pName);
            //System.out.println("Input: " + pName);
            boolean a = nodeMap.get(pName) != null; //&& nodeMap.get(pName).nodeType().equals(nodeType);

            System.out.println("findInputNodesWithType ::   loop bool " + a);
            if (a) {
                TypeMirror b = stringTypeMirrorMap.get(pName);
                System.out.println("findInputNodesWithType ::   in if " + b);
                nodes.add(b);
            }
        }

        System.out.println("findInputNodesWithType :: returning " + nodes);

        return nodes;
    }

    private String simpleName(TypeMirror typeMirror) {
        return typeMirror.toString().substring(typeMirror.toString().lastIndexOf('.') + 1);
    }

    private JavaFile generateDBQueryInterface(TypeMirror dbType, TypeMirror processor) {
        System.out.println("1");
        System.out.println(dbType.toString());
        System.out.println(processor.toString());

        ClassName paramClassName = ClassName.bestGuess(outputTypes.get(dbType).toString());

        System.out.println("2");

        MethodSpec methodSpec = MethodSpec
                .methodBuilder("query")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(paramClassName, "db")
                .returns(Object.class) // This will change later with the Query annotation
                .build();

        System.out.println("3");

        String interfaceName = dbType.toString().substring(dbType.toString().lastIndexOf('.') + 1) +
                "To" +
                processor.toString().substring(processor.toString().lastIndexOf('.') + 1) +
                "Query";

        System.out.println("4");

        TypeSpec anInterface = TypeSpec
                .interfaceBuilder("I" + interfaceName)
                .addMethod(methodSpec)
                .addModifiers(Modifier.PUBLIC)
                .build();

        System.out.println("5");

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

    public TypeMirror findTarget(TypeElement element) {
        Node n = nodeMap.get(element.getSimpleName().toString());

        System.out.println("findTarget :: finding for " + element.getQualifiedName());

        // TODO: Is stepping two steps forward a good thing?
        //  It's mentioned that the developers are supposed to be able to edit the PADFD. What happens then?

        List<Node> outputs1 = n.outputs();

        for (Node a : outputs1) {
            System.out.println("    First loop: " + a.name());
            // all a should be limits or requests
            // TODO: Second loop is only relevant for Limits. Maybe add if statement
            List<Node> outputs2 = a.outputs();
            for (Node b : outputs2) {
                System.out.println("        Second loop: " + b.name());
                TypeMirror outputTargetType = stringTypeMirrorMap.get(b.name());
                // if it is null, that means that it's a log, limit, reason, etc
                if (outputTargetType != null) {
                    return outputTargetType;
                }
            }
        }


        System.out.println("findTarget :: returning null :( ");


        return null;
    }
}
