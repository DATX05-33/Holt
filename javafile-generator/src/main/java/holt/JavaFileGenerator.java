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

import static holt.DatabaseJavaFileGenerator.generate;
import static holt.ExternalEntityJavaFileGenerator.generate;
import static holt.ProcessJavaFileGenerator.generate;

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
        List<JavaFile> javaFiles = convertToJavaFiles(domain, processingEnv);
        for (JavaFile javaFile : javaFiles) {
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static List<JavaFile> convertToJavaFiles(Domain domain, ProcessingEnvironment processingEnvironment) {
        String dfdPackageName = packageOf(domain);
        List<JavaFile> javaFiles = new ArrayList<>();

        javaFiles.addAll(PrivacyActivatorJavaFileGenerator.convertToJavaFiles(domain, processingEnvironment));

        domain
                .databases()
                .forEach(database -> javaFiles.add(generate(database, dfdPackageName)));

        domain
                .externalEntities()
                .forEach(externalEntity -> javaFiles.add(generate(domain, dfdPackageName, externalEntity)));

        domain
                .processes()
                .forEach(process -> javaFiles.addAll(generate(process, dfdPackageName, domain)));

        return javaFiles;
    }

    public static AnnotationSpec getGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("\"" + JavaFileGenerator.class.getName() + "\""))
                .build();
    }

    public static TypeName toTypeName(QualifiedName qualifiedName) {
        if (qualifiedName.types() == null) {
            return ClassName.bestGuess(qualifiedName.value());
        } else{
            TypeName[] types = new TypeName[qualifiedName.types().size()];
            List<QualifiedName> qualifiedNames = qualifiedName.types();
            for (int i = 0; i < qualifiedNames.size(); i++) {
                QualifiedName type = qualifiedNames.get(i);
                types[i] = ClassName.bestGuess(type.value());
            }
            return ParameterizedTypeName.get(
                    ClassName.bestGuess(qualifiedName.value()),
                    types
            );
        }
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
