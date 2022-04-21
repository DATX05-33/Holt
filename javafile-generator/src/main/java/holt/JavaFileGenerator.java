package holt;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import holt.activator.Connector;
import holt.activator.Domain;
import holt.activator.FlowOutput;
import holt.activator.QualifiedName;
import holt.activator.TraverseOutput;

import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JavaFileGenerator {

    public static final String PACKAGE_NAME = "holt.processor.generation";

    public static List<MethodSpec> toOutputMethods(List<TraverseOutput> outputs) {
        List<MethodSpec> outputMethods = new ArrayList<>();
        outputs.forEach(traverseOutput -> {
            var methodSpecBuilder = MethodSpec.methodBuilder(traverseOutput.functionName().value())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

            for (int i = 0; i < traverseOutput.inputs().size(); i++) {
                Connector connector = traverseOutput.inputs().get(i);
                methodSpecBuilder.addParameter(toTypeName(connector), "input" + i);
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
        String dfdPackageName = packageOf(domain);
        List<JavaFile> javaFiles = new ArrayList<>();

        domain
                .databases()
                .forEach(database -> javaFiles.add(DatabaseJavaFileGenerator.generate(database, dfdPackageName)));

        domain
                .externalEntities()
                .forEach(externalEntity -> javaFiles.add(ExternalEntityJavaFileGenerator.generate(domain, dfdPackageName, externalEntity)));

        domain
                .processes()
                .forEach(process -> javaFiles.addAll(
                        ProcessJavaFileGenerator.generate(process, dfdPackageName)
                ));

        return javaFiles;
    }

    public static AnnotationSpec getGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("\"" + JavaFileGenerator.class.getName() + "\""))
                .build();
    }

    public static TypeName toTypeName(QualifiedName qualifiedName) {
        return ClassName.bestGuess(qualifiedName.value());
    }

    public static TypeName toTypeName(FlowOutput flowOutput) {
        TypeName connectorTypeName = toTypeName(flowOutput.type());
        if (flowOutput.isCollection()) {
            ClassName collection = ClassName.get(Collection.class);
            return ParameterizedTypeName.get(collection, connectorTypeName);
        } else {
            return connectorTypeName;
        }
    }

    public static TypeName toTypeName(Connector connector) {
        return toTypeName(connector.flowOutput());
    }

    public static TypeName toTypeName(String qualifiedName) {
        return ClassName.bestGuess(qualifiedName);
    }

    public static String packageOf(Domain domain) {
        return PACKAGE_NAME + "." + domain.name().value();
    }
}
