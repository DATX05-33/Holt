package holt.processor.generation;

import com.squareup.javapoet.*;
import holt.processor.DFDParser;
import holt.processor.Dataflow;
import holt.processor.Node;
import holt.processor.NodeType;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator {

    // For each flow, keep track of the input types for every class (every class only has one method per flow)
    private final Map<String, Map<TypeMirror, List<TypeMirror>>> inputTypes = new HashMap<>();
    // for each flow, keep track of the output type for each class (every class only has one method per flow)
    private final Map<String, Map<TypeMirror, TypeMirror>> outputTypes = new HashMap<>();
    // i'm not really sure how this is working. Shouldn't it be one of these Maps for each flow?
    // the function name for each class
    private final Map<TypeMirror, String> functionNames = new HashMap<>();

    // list of interfaces, used for appending more methods when new flows are handled
    private final Map<String, TypeSpec.Builder> interfaces = new HashMap<>();

    // used to access the TypeMirror when only the name is available
    private final Map<String, TypeMirror> nameToTypeMirrorMap = new HashMap<>();

    private final String PACKAGE_NAME = "holt.processor.generation.interfaces";

    // used to get the Node when only the name is available
    private final Map<String, Node> nodeMap = new HashMap<>();

    private DFDParser.DFD dfd;

    // singleton
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

    public void setDFD(DFDParser.DFD dfd) {
        this.dfd = dfd;

        for (Node n : dfd.processes()) {
            nodeMap.put(n.name(), n);
        }

        for (Node n : dfd.databases()) {
            nodeMap.put(n.name(), n);
        }

        for (Node n : dfd.externalEntities()) {
            nodeMap.put(n.name(), n);
        }
    }

    public void addTypeMirror(String name, TypeMirror typeMirror) {
        nameToTypeMirrorMap.put(name, typeMirror);
    }

    public void addOutputTypeAndFunctionName(String flow, TypeMirror source, TypeMirror outputType, TypeMirror target, String functionName) {
        if (outputTypes.get(flow) == null) {
            Map<TypeMirror, TypeMirror> map = new HashMap<>();
            map.put(source, outputType);

            outputTypes.put(flow, map);
        } else {
            outputTypes.get(flow).put(source, outputType);
        }

        functionNames.put(source, functionName);

        // add the source's output as the input for the target
        addInputTypes(flow, outputType, target);
    }

    public void addOutputType(String flow, TypeMirror source, TypeMirror outputType, TypeMirror target) {
        if (outputTypes.get(flow) == null) {
            Map<TypeMirror, TypeMirror> map = new HashMap<>();
            map.put(source, outputType);

            outputTypes.put(flow, map);
        } else {
            outputTypes.get(flow).put(source, outputType);
        }

        // add the source's output as the input for the target
        addInputTypes(flow, outputType, target);
    }

    public void addInputTypes(String flow, TypeMirror inputType, TypeMirror source) {
        // this could probably be prettier. Code duplication?
        if (inputTypes.get(flow) == null) {
            List<TypeMirror> list = new ArrayList<>();
            list.add(inputType);
            Map<TypeMirror, List<TypeMirror>> map = new HashMap<>();

            map.put(source, list);
            inputTypes.put(flow, map);
        } else {
            List<TypeMirror> a = inputTypes.get(flow).get(source);
            if (a == null) {
                List<TypeMirror> list = new ArrayList<>();
                list.add(inputType);
                inputTypes.get(flow).put(source, list);
            }
        }
    }

    public List<JavaFile> generateInterfaces() {
        // this is where the magic happens
        for (String flow : outputTypes.keySet()) {
            // for each flow
            for (TypeMirror currentProcess : outputTypes.get(flow).keySet()) {
                String currentSimpleName = simpleName(currentProcess);
                Node currentNode = nodeMap.get(currentSimpleName);

                // Check if currentNode is External, DB or process
                switch (currentNode.nodeType()) {
                    case EXTERNAL_ENTITY -> {
                        interfaces.put(currentSimpleName, addAbstractExternalEntityFlow(flow, currentProcess));
                    }
                    default -> {
                        interfaces.put(currentSimpleName, addProcessFlow(flow, currentProcess));
                    }
                }
            }
        }

        // Build all the things!
        return interfaces.values().stream().map(e -> JavaFile.builder(PACKAGE_NAME, e.build()).build()).toList();
    }

    private TypeSpec.Builder addProcessFlow(String flow, TypeMirror currentProcess) {
        // this basically adds a method to the interface (might also create the interface if it does not exist)

        String currentSimpleName = simpleName(currentProcess);

        // if the interface does not exist, create it
        TypeSpec.Builder anInterfaceBuilder = interfaces.get(currentSimpleName);
        if (anInterfaceBuilder == null) {
            anInterfaceBuilder = TypeSpec
                    .interfaceBuilder("I" + currentSimpleName)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        }

        MethodSpec.Builder methodSpecBuilder = MethodSpec
                .methodBuilder(functionNames.get(currentProcess))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        // add input to the method
        if (inputTypes.get(flow).get(currentProcess) != null) {
            for (int i = 0; i < inputTypes.get(flow).get(currentProcess).size(); i++) {
                ClassName parameterClassName = ClassName.bestGuess(inputTypes.get(flow).get(currentProcess).get(i).toString());
                methodSpecBuilder.addParameter(
                        parameterClassName,
                        "input" + i
                );
            }
        }

        // add database connection
        if (nodeMap.get(currentSimpleName).nodeType().equals(NodeType.PROCESS)) {
            List<TypeMirror> dbTypes = findDatabaseInput(flow, currentProcess);

            for (int i = 0; i < dbTypes.size(); i++) {
                methodSpecBuilder.addParameter(
                        Object.class,
                        "input" + (i+1)
                );
            }

            // for each database that's connected to this process
            for (TypeMirror db : dbTypes) {
                TypeSpec.Builder DBQuery = generateDBQueryInterface(db, currentProcess);
                interfaces.put(DBQuery.build().name, DBQuery);

                ClassName returnClass = ClassName.bestGuess( PACKAGE_NAME + "." + DBQuery.build().name);
                ClassName parameterClass = ClassName.bestGuess(inputTypes.get(flow).get(currentProcess).get(0).toString());
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

        ClassName returnClassName = ClassName.bestGuess(outputTypes.get(flow).get(currentProcess).toString());
        methodSpecBuilder.returns(returnClassName);

        MethodSpec methodSpec = methodSpecBuilder.build();

        anInterfaceBuilder
                .addMethod(methodSpec);

        return anInterfaceBuilder;
    }

    private TypeSpec.Builder addAbstractExternalEntityFlow(String flow, TypeMirror currentProcess) {
        String currentSimpleName = simpleName(currentProcess);

        // if the abstract class does not exist, create it
        TypeSpec.Builder anAbstractBuilder = interfaces.get(currentSimpleName);
        if (anAbstractBuilder == null) {
            anAbstractBuilder = TypeSpec
                    .classBuilder(currentSimpleName)
                    .addModifiers(Modifier.PUBLIC)
                    .addModifiers(Modifier.ABSTRACT);
        }

        CodeBlock comment = CodeBlock.builder().add("// TODO: Call Holt?").build();

        MethodSpec methodSpec = MethodSpec
                .methodBuilder(flow)
                .addModifiers(Modifier.PUBLIC)
                .addCode(comment)
                .build();

        anAbstractBuilder.addMethod(methodSpec);

        return anAbstractBuilder;
    }

    private List<TypeMirror> findDatabaseInput(String flow, TypeMirror process) {
        List<TypeMirror> databases = new ArrayList<>();
        Node node = nodeMap.get(simpleName(process));
        List<Node> inputs = DFDParser.nodeInputs(dfd, flow, node);

        for (Node n : inputs) {
            if (n.nodeType().equals(NodeType.DATA_BASE)) {
                databases.add(nameToTypeMirrorMap.get(n.name()));
            }
        }

        return databases;
    }

    private String simpleName(TypeMirror typeMirror) {
        return typeMirror.toString().substring(typeMirror.toString().lastIndexOf('.') + 1);
    }

    private TypeSpec.Builder generateDBQueryInterface(TypeMirror dbType, TypeMirror processor) {
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

        TypeSpec.Builder anInterface = TypeSpec
                .interfaceBuilder("I" + interfaceName)
                .addMethod(methodSpec)
                .addModifiers(Modifier.PUBLIC);

        return anInterface;
    }

    public TypeMirror findTarget(String flow, TypeElement element) {
        Node n = nodeMap.get(element.getSimpleName().toString());

        List<Dataflow> dataflows = dfd.flowsMap().get(flow);

        for (Dataflow d : dataflows) {
            if (d.from().equals(n)) {
                TypeMirror i = nameToTypeMirrorMap.get(d.to().name());
                return i;
            }
        }

        // TODO: throw error? This should not happen
        return null;
    }
}
