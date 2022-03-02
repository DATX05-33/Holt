package holt.processor;

import com.squareup.javapoet.JavaFile;
import holt.processor.annotation.Query;
import holt.processor.generation.CodeGenerator;

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

public class QueryProcessor extends AbstractProcessor {

    private static final String annotationName = Query.class.getName();

    private final CodeGenerator codeGenerator = CodeGenerator.getInstance();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(annotationName);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }



    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
        if (!env.processingOver()) {
            for (Element element : env.getElementsAnnotatedWith(Query.class)) {
                queryProcess(element);
            }
        }

        return true;
    }

    private void queryProcess(Element element) {
        Query annotation = element.getAnnotation(Query.class);

        TypeElement output = asTypeElement(
                getMyValue(element, annotation,"value")
        );
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

    private void saveJavaFile(JavaFile javaFile) {
        try {
            if (javaFile != null) {
                javaFile.writeTo(processingEnv.getFiler());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
