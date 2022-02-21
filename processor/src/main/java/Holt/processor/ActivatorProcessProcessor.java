package Holt.processor;

import Holt.processor.annotation.ActivatorProcess;
import Holt.processor.generation.CodeGeneration;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

public class ActivatorProcessProcessor extends AbstractProcessor {

    private static final String annotationName = ActivatorProcess.class.getName();


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(annotationName);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (!env.processingOver()) {
            for (Element typeElement : env.getElementsAnnotatedWith(ActivatorProcess.class)) {
                try {
                    activatorProcess(typeElement);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    /**
     * Add methods to be implemented in interface with the same name as the class
     * <p>
     * https://stackoverflow.com/questions/7687829/java-6-annotation-processing-getting-a-class-from-an-annotation/10167558#10167558
     *
     * @param typeElement class
     */
    private void activatorProcess(Element typeElement) throws ClassNotFoundException {
        ActivatorProcess annotation = typeElement.getAnnotation(ActivatorProcess.class);


        System.out.println("one");
        var a = getMyValue(typeElement, annotation, "input");
        TypeElement input = asTypeElement(a);

        System.out.println("two");

        var b = getMyValue(typeElement, annotation, "output");
        TypeElement output = asTypeElement(b);

        JavaFile javaFile = CodeGeneration.generateMethodFromAnnotation(
                typeElement,
                Class.forName(String.valueOf(input.getQualifiedName())),
                Class.forName(String.valueOf(output.getQualifiedName())),
                annotation.methodName()
        );

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypeElement asTypeElement(TypeMirror typeMirror) {
        Types TypeUtils = this.processingEnv.getTypeUtils();
        return (TypeElement) TypeUtils.asElement(typeMirror);
    }

    private static AnnotationMirror getAnnotationMirror(Element typeElement, Class<?> clazz) {
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
    
    public TypeMirror getMyValue(Element foo, Annotation annotation, String key) {
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
}
