package holt.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import holt.processor.activator.Activators;
import holt.processor.activator.Connector;
import holt.processor.activator.Database;
import holt.processor.activator.ExternalEntity;
import holt.processor.activator.Flow;
import holt.processor.activator.FlowName;
import holt.processor.activator.Process;
import holt.processor.activator.QueryConnector;
import holt.processor.annotation.FlowStartRep;
import holt.processor.annotation.FlowThroughRep;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static holt.processor.DFDsProcessor.DATABASE_PREFIX;
import static holt.processor.DFDsProcessor.EXTERNAL_ENTITY_PREFIX;
import static holt.processor.DFDsProcessor.PACKAGE_NAME;
import static holt.processor.DFDsProcessor.PROCESS_PREFIX;

public class DFDToJavaFileConverter {

    private final DFDName dfdName;
    private final Activators activators;
    private final String dfdPackageName;

    public DFDToJavaFileConverter(DFDName dfdName, Activators activators) {
        this.dfdName = dfdName;
        this.activators = activators;
        this.dfdPackageName = PACKAGE_NAME + "." + dfdName;
    }

    public void applyFlowStarts(List<FlowStartRep> flowStartReps) {
        for (FlowStartRep flowStart : flowStartReps) {
            flowStart.externalEntity().setOutputType(flowStart.flowName(), flowStart.flowStartType());
        }
    }

    public void applyFlowThrough(List<FlowThroughRep> flowThroughReps) {
        for (FlowThroughRep flowThrough : flowThroughReps) {
            Flow flow = flowThrough.process().getFlow(flowThrough.flowName());
            flow.setOutputType(flowThrough.outputType());
            flow.setFunctionName(flowThrough.functionName());

            flowThrough.queries().forEach(query -> {
                for (Connector input : flow.inputs()) {
                    if (input instanceof QueryConnector inputQueryConnector) {
                        Database database = inputQueryConnector.database();
                        if ((database.name().value()).equals(query.db().simpleName())) {
                            inputQueryConnector.setType(query.type());
                        }
                    }
                }
            });
        }
    }

    public List<JavaFile> convertToJavaFiles() {
        List<JavaFile> javaFiles = new ArrayList<>();
        Map<Database, JavaFile> databaseMap = new HashMap<>();

        activators
                .databases()
                .forEach(database -> {
                    JavaFile databaseJavaFile = this.generateDatabaseJavaFile(database);

                    javaFiles.add(databaseJavaFile);
                    databaseMap.put(database, databaseJavaFile);
                });

        activators
                .externalEntities()
                .forEach(externalEntity -> javaFiles.add(this.generateExternalEntityJavaFile(externalEntity)));

        activators
                .processes()
                .forEach(process -> javaFiles.addAll(
                        this.generateProcessJavaFile(process, databaseMap)
                ));

        return javaFiles;
    }

    public DFDName getDFDName() {
        return this.dfdName;
    }

    private List<JavaFile> generateProcessJavaFile(Process process, Map<Database, JavaFile> databaseMap) {
        List<JavaFile> newFiles = new ArrayList<>();

        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(PROCESS_PREFIX + process.name())
                .addModifiers(Modifier.PUBLIC);

        for (Flow flow : process.getFlows()) {
            MethodSpec.Builder methodSpecBuilder = MethodSpec
                    .methodBuilder(flow.functionName())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

            int i = 0;
            for (Connector connector : flow.inputs()) {
                ClassName parameterClassName = connector.type();

                String parameterName = "input" + i;
                if (connector instanceof QueryConnector) {
                    parameterName = "dbInput" + i;
                }

                methodSpecBuilder.addParameter(parameterClassName, parameterName);
                i++;
            }

            // Databases
            for (Connector input : flow.inputs()) {
                if (input instanceof QueryConnector queryInput) {
                    // First add query interface
                    String databaseName = databaseMap.get(queryInput.database()).typeSpec.name;
                    ClassName databaseClassname = ClassName.bestGuess(dfdPackageName + "." + databaseName);
                    TypeSpec queryInterfaceSpec = generateQuery(queryInput, databaseName + "To" + process.name() + flow.functionName() + "Query", databaseClassname);
                    newFiles.add(JavaFile.builder(dfdPackageName, queryInterfaceSpec).build());

                    // Then add method to create that interface
                    ClassName returnClass = ClassName.bestGuess(dfdPackageName + "." + queryInterfaceSpec.name);
                    MethodSpec.Builder queryMethodSpecBuilder = MethodSpec
                            .methodBuilder("query_" + queryInput.database().name() + "_" + flow.functionName())
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .returns(returnClass);

                    int j = 0;
                    for (Connector input2 : flow.inputs()) {
                        if (!(input2 instanceof QueryConnector)) {
                            ClassName parameterClassName = input2.type();
                            queryMethodSpecBuilder.addParameter(
                                    parameterClassName,
                                    "input" + j
                            );
                            j++;
                        }
                    }

                    interfaceBuilder.addMethod(queryMethodSpecBuilder.build());
                }
            }

            ClassName returnClassName = flow.output().type();
            methodSpecBuilder.returns(returnClassName);

            interfaceBuilder.addMethod(methodSpecBuilder.build());
        }

        newFiles.add(JavaFile.builder(dfdPackageName, interfaceBuilder.build()).build());

        return newFiles;
    }

    private JavaFile generateExternalEntityJavaFile(ExternalEntity externalEntity) {
        TypeSpec.Builder externalEntityBuilder = TypeSpec
                .classBuilder(EXTERNAL_ENTITY_PREFIX + externalEntity.name())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        externalEntity
                .starts()
                .entrySet()
                .stream()
                .map(flowNameFlowEntry ->
                        this.generateExternalEntityStartMethod(
                                externalEntity,
                                flowNameFlowEntry.getKey(),
                                flowNameFlowEntry.getValue()
                        )
                )
                .forEach(externalEntityBuilder::addMethod);

        return JavaFile.builder(dfdPackageName, externalEntityBuilder.build()).build();
    }

    private MethodSpec generateExternalEntityStartMethod(ExternalEntity externalEntity, FlowName flowName, Flow startFlow) {
        ClassName parameterClassType = startFlow.output().type();
        ParameterSpec dataParameterSpec = ParameterSpec.builder(parameterClassType, "d").build();
        ParameterSpec policyParameterSpec = ParameterSpec.builder(Object.class, "pol").build();

        CodeBlock comment = CodeBlock.builder().add("// TODO: Call Holt?\n").build();

        MethodSpec.Builder methodSpecBuilder = MethodSpec
                .methodBuilder(flowName.value())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addCode(comment)
                .addParameter(dataParameterSpec)
                .addParameter(policyParameterSpec);

        externalEntity.end(flowName)
                .ifPresent(flow -> {
                    CodeBlock returnStatement = CodeBlock.builder().add("return null;").build();
                    methodSpecBuilder.addCode(returnStatement);
                    ClassName returnClassType = flow.type();
                    methodSpecBuilder.returns(returnClassType);
                });

        return methodSpecBuilder.build();
    }

    private JavaFile generateDatabaseJavaFile(Database database) {
        TypeSpec databaseSpec = TypeSpec.interfaceBuilder(DATABASE_PREFIX + database.name())
                .addModifiers(Modifier.PUBLIC)
                .build();

        return JavaFile.builder(dfdPackageName, databaseSpec).build();
    }

    private TypeSpec generateQuery(QueryConnector queryConnector, String queryInterfaceName, ClassName databaseClassname) {
        ClassName returnQueryType = queryConnector.type();

        MethodSpec queryMethod = MethodSpec
                .methodBuilder("createQuery")
                .addParameter(databaseClassname, "db")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(returnQueryType)
                .build();

        return TypeSpec
                .interfaceBuilder(queryInterfaceName)
                .addMethod(queryMethod)
                .addModifiers(Modifier.PUBLIC)
                .build();
    }

}
