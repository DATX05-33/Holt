package holt;

import com.squareup.javapoet.*;
import holt.activator.Connector;
import holt.activator.DFDName;
import holt.activator.DatabaseActivatorAggregate;
import holt.activator.Domain;
import holt.activator.ExternalEntityActivatorAggregate;
import holt.activator.FlowThroughAggregate;
import holt.activator.FunctionName;
import holt.activator.ProcessActivatorAggregate;
import holt.activator.QualifiedName;
import holt.activator.QueryInput;
import holt.activator.QueryInputDefinition;
import holt.activator.TraverseName;
import holt.activator.TraverseOutput;
import holt.applier.FlowThroughRep;
import holt.applier.QueriesForRep;
import holt.applier.QueryDefinitionRep;
import holt.applier.TraverseRep;

import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaFileGenerator {

    private static final String PACKAGE_NAME = "holt.processor.generation";

    public static List<MethodSpec> toOutputMethods(List<TraverseOutput> outputs) {
        List<MethodSpec> outputMethods = new ArrayList<>();
        outputs.forEach(traverseOutput -> {
            var methodSpecBuilder = MethodSpec.methodBuilder(traverseOutput.functionName().value())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

            for (int i = 0; i < traverseOutput.inputs().size(); i++) {
                Connector connector = traverseOutput.inputs().get(i);
                methodSpecBuilder.addParameter(toClassName(connector.type()), "input" + i);
            }

            outputMethods.add(methodSpecBuilder.build());
        });

        return outputMethods;
    }

    public static void saveJavaFiles(Domain domain, ProcessingEnvironment processingEnv) {
        List<JavaFile> javaFiles = convertToJavaFiles(domain);
        for (JavaFile javaFile : javaFiles) {
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static List<JavaFile> convertToJavaFiles(Domain domain) {
        String dfdPackageName = PACKAGE_NAME + "." + domain.name();
        List<JavaFile> javaFiles = new ArrayList<>();
        Map<DatabaseActivatorAggregate, JavaFile> databaseMap = new HashMap<>();

        domain
                .databases()
                .forEach(database -> {
                    JavaFile databaseJavaFile = DatabaseJavaFileGenerator.generate(database, dfdPackageName);

                    javaFiles.add(databaseJavaFile);
                    databaseMap.put(database, databaseJavaFile);
                });

        domain
                .externalEntities()
                .forEach(externalEntity -> javaFiles.add(ExternalEntityJavaFileGenerator.generate(domain, dfdPackageName, externalEntity)));

        domain
                .processes()
                .forEach(process -> javaFiles.addAll(
                        ProcessJavaFileGenerator.generate(process, dfdPackageName, databaseMap)
                ));

        return javaFiles;
    }

    public static AnnotationSpec getGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("\"holt.processor.DFDsProcessor\""))
                .build();
    }

    public static ClassName toClassName(QualifiedName qualifiedName) {
        return ClassName.bestGuess(qualifiedName.value());
    }

    public static ClassName toClassName(String qualifiedName) {
        return ClassName.bestGuess(qualifiedName);
    }
}
