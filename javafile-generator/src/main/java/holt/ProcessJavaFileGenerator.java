package holt;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import holt.activator.Connector;
import holt.activator.DatabaseActivatorAggregate;
import holt.activator.FlowThroughAggregate;
import holt.activator.ProcessActivatorAggregate;
import holt.activator.QueryInput;
import holt.activator.QueryInputDefinition;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

import static holt.JavaFileGenerator.getGeneratedAnnotation;
import static holt.JavaFileGenerator.toTypeName;

public final class ProcessJavaFileGenerator {

    private ProcessJavaFileGenerator() { }

    public static List<JavaFile> generate(ProcessActivatorAggregate processActivator, String dfdPackageName) {
        List<JavaFile> newFiles = new ArrayList<>();

        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(processActivator.requirementsName().value())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(getGeneratedAnnotation());

        for (FlowThroughAggregate flowThrough : processActivator.flows()) {
            MethodSpec.Builder methodSpecBuilder = MethodSpec
                    .methodBuilder(flowThrough.functionName().value())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

            int i = 0;
            for (Connector connector : flowThrough.inputs()) {
                TypeName parameterClassName = toTypeName(connector);
                methodSpecBuilder.addParameter(
                        parameterClassName,
                        "input" + i
                );
                i++;
            }

            for (QueryInput queryInput : flowThrough.queries()) {
                TypeName parameterTypeName = toTypeName(queryInput.queryInputDefinition().output());
                methodSpecBuilder.addParameter(parameterTypeName, "dbInput" + i);
                i++;
            }

            TypeName returnClassName = JavaFileGenerator.toTypeName(flowThrough.output());
            methodSpecBuilder.returns(returnClassName);

            interfaceBuilder.addMethod(methodSpecBuilder.build());

            // Databases queries definitions
            for (QueryInputDefinition queryInputDefinition : flowThrough.queryInputDefinitions()) {
                // Database that is going to be queried from
                DatabaseActivatorAggregate database = queryInputDefinition.database();

                // Find what the type of db should be used. Either querier, db or db requirements
                TypeName databaseTypeName;
                if (database.getQueriesClassName() != null) {
                    databaseTypeName = toTypeName(database.getQueriesClassName());
                } else if (database.connectedClass().isPresent()) {
                    databaseTypeName = toTypeName(database.connectedClass().get().qualifiedName());
                } else {
                    String databaseRequirementsName = queryInputDefinition.database().requirementsName().value();
                    databaseTypeName = toTypeName(dfdPackageName + "." + databaseRequirementsName);
                }

                String queryInterfaceName = processActivator.getQueryInterfaceNameForDatabase(database, flowThrough);

                // Generate the query interface
                TypeSpec queryInterfaceSpec = generateQuery(queryInputDefinition, queryInterfaceName, databaseTypeName);
                newFiles.add(JavaFile.builder(dfdPackageName, queryInterfaceSpec).build());

                ClassName returnClass = ClassName.bestGuess(dfdPackageName + "." + queryInterfaceSpec.name);
                String queryMethodName = processActivator.getQueryMethodNameForDatabase(database, flowThrough);
                MethodSpec.Builder queryMethodSpecBuilder = MethodSpec
                        .methodBuilder(queryMethodName)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(returnClass);

                List<Connector> inputsForQueryDefinition = flowThrough.inputs();
                for (int j = 0; j < inputsForQueryDefinition.size(); j++) {
                    Connector connector = inputsForQueryDefinition.get(j);
                    queryMethodSpecBuilder.addParameter(
                            toTypeName(connector),
                            "input" + j
                    );
                }

                interfaceBuilder.addMethod(queryMethodSpecBuilder.build());
            }
        }

        newFiles.add(JavaFile.builder(dfdPackageName, interfaceBuilder.build()).build());

        return newFiles;
    }

    private static TypeSpec generateQuery(QueryInputDefinition queryInputDefinition, String queryInterfaceName, TypeName databaseTypeName) {
        TypeName returnQueryType = toTypeName(queryInputDefinition.output());

        MethodSpec queryMethod = MethodSpec
                .methodBuilder("createQuery")
                .addParameter(databaseTypeName, "db")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(returnQueryType)
                .build();

        return TypeSpec
                .interfaceBuilder(queryInterfaceName)
                .addAnnotation(getGeneratedAnnotation())
                .addMethod(queryMethod)
                .addModifiers(Modifier.PUBLIC)
                .build();
    }


}
