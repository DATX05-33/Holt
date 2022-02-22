package Holt.processor;

import Holt.processor.annotation.Activator;
import Holt.processor.generation.CodeGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

public class ActivatorProcessProcessor extends AbstractProcessor {

    private static final String annotationName = Activator.class.getName();

    private final CodeGenerator codeGenerator = new CodeGenerator();


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
            for (Element element : env.getElementsAnnotatedWith(Activator.class)) {
                if (element instanceof TypeElement typeElement) {
                    try {
                        activatorProcess(typeElement);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Element was not TypeElement");
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
     * @param element class
     */
    private void activatorProcess(TypeElement element) throws ClassNotFoundException {
        Activator annotation = element.getAnnotation(Activator.class);

        TypeElement output = asTypeElement(
                getMyValue(element, annotation,"outputType")
        );

        String sourceString = String.valueOf(element.getQualifiedName());
        String outputString = String.valueOf(output.getQualifiedName());

        // TODO: Can't load that class.
        //  Can we load the interface, which is what matters?
        //  It's this method that generates it, maybe need to generate a stub before?
        Class<?> source = Class.forName(sourceString);

        // TODO: How do we know if the type has an interface that we've made or not? Maybe try to load the class and catch the ClassNotFound and load our interface
        Class<?> outputType = Class.forName(outputString);

        // TODO: Find via graph. How do we get the actual class from the node name? The name should be unique and found in the interface package
        Class<?> target = null;
        String methodName = annotation.methodName();

        codeGenerator.addOutputTypeAndFunctionName(source, outputType, target, methodName);
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
}
