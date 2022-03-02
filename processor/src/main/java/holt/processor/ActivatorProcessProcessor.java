package holt.processor;

import com.squareup.javapoet.JavaFile;
import holt.processor.annotation.Activator;
import holt.processor.annotation.DBActivator;
import holt.processor.generation.CodeGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivatorProcessProcessor extends AbstractProcessor {

    private static final String annotationName1 = Activator.class.getName();
    private static final String annotationName2 = DBActivator.class.getName();

    private final CodeGenerator codeGenerator = CodeGenerator.getInstance();



    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(annotationName1, annotationName2);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private boolean firstRun = true;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (!env.processingOver() && firstRun) {
            // first pass we map String to TypeMirrors
            for (Element element : env.getElementsAnnotatedWith(Activator.class)) {
                if (element instanceof TypeElement typeElement) {
                    // all SimpleNames have to be unique and same as the node name in the PADFD
                    addTypeMirrors(typeElement);
                } else {
                    System.out.println("Element was not TypeElement");
                }
            }

            for (Element element : env.getElementsAnnotatedWith(DBActivator.class)) {
                if (element instanceof TypeElement typeElement) {
                    addDBTypeMirrors(typeElement);
                } else {
                    System.out.println("Element was not TypeElement");
                }
            }

            // then we map the outputs and inputs
            for (Element element : env.getElementsAnnotatedWith(Activator.class)) {
                if (element instanceof TypeElement typeElement) {
                    mapInputOutput(typeElement);
                } else {
                    System.out.println("Element was not TypeElement");
                }
            }

            List<JavaFile> interfaces = codeGenerator.generateInterfaces();

            for (JavaFile j : interfaces) {
                saveJavaFile(j);
            }

            firstRun = false;   // processor runs in multiple runs. We only need the first one
                                // TODO: Double check that all necessary processing happens in round one
                                //  maybe somehow mark the elements as processed?
        }

        return true;
    }

    private void addTypeMirrors(TypeElement typeElement) {
        Activator annotation = typeElement.getAnnotation(Activator.class);

        TypeElement output = asTypeElement(
                getMyValue(typeElement, annotation,"outputType")
        );

        codeGenerator.addTypeMirror(typeElement.getSimpleName().toString(), typeElement.asType());
        codeGenerator.addTypeMirror(output.getSimpleName().toString(), output.asType());
    }

    private void addDBTypeMirrors(TypeElement typeElement) {
        DBActivator annotation = typeElement.getAnnotation(DBActivator.class);

        codeGenerator.addTypeMirror(typeElement.getSimpleName().toString(), typeElement.asType());
    }

    /**
     * Add methods to be implemented in interface with the same name as the class
     * <p>
     * https://stackoverflow.com/questions/7687829/java-6-annotation-processing-getting-a-class-from-an-annotation/10167558#10167558
     *
     * @param element class
     */
    private void mapInputOutput(TypeElement element)  {
        Activator annotation = element.getAnnotation(Activator.class);

        TypeElement output = asTypeElement(
                getMyValue(element, annotation,"outputType")
        );

        String methodName = annotation.methodName();

        // TODO: Generate DBQuery interface.
        //  interface FriendDBToFormatFriendDBQuery { Object query(FriendDB db);

        TypeMirror target = codeGenerator.findTarget(element);

        codeGenerator.addOutputTypeAndFunctionName(element.asType(), output.asType(), target, methodName);
    }

    private TypeElement asTypeElement(TypeMirror typeMirror) {
        Types TypeUtils = this.processingEnv.getTypeUtils();
        return (TypeElement) TypeUtils.asElement(typeMirror);
    }

    private static AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> clazz) {
        String clazzName = clazz.getName();
        for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            var a = m.getAnnotationType().toString();
            if (m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public TypeMirror getMyValue(TypeElement foo, Annotation annotation, String key) {
        AnnotationMirror am = getAnnotationMirror(foo, annotation.annotationType());
        if (am == null) {
            return null;
        }
        AnnotationValue av = getAnnotationValue(am, key);
        if (av == null) {
            return null;
        } else {
            return (TypeMirror) av.getValue();
        }
    }

    private void saveJavaFile(JavaFile javaFile) {
        try {
            if (javaFile != null) {
                String fullyQualifiedName = javaFile.packageName + "." + javaFile.typeSpec.name;
                System.out.println("Trying to create " + fullyQualifiedName);
                JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(fullyQualifiedName);
                if (sourceFile.delete()) {
                    System.out.println("DELETED");
                }
                try (Writer writer = sourceFile.openWriter()) {
                    writer.write(javaFile.toString());
                }

                //javaFile.writeTo(processingEnv.getFiler());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
