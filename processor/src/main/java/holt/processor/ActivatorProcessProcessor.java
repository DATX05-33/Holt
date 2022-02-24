package holt.processor;

import com.squareup.javapoet.JavaFile;
import holt.processor.annotation.Activator;
import holt.processor.generation.CodeGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivatorProcessProcessor extends AbstractProcessor {

    private static final String annotationName = Activator.class.getName();

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
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (!env.processingOver()) {
            for (Element element : env.getElementsAnnotatedWith(Activator.class)) {
                if (element instanceof TypeElement typeElement) {
                    activatorProcess(typeElement);
                } else {
                    System.out.println("Element was not TypeElement");
                }
            }
        }

        List<JavaFile> interfaces = codeGenerator.generateInterfaces();

        for (JavaFile j : interfaces) {
            saveJavaFile(j);
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
    private void activatorProcess(TypeElement element)  {
        Activator annotation = element.getAnnotation(Activator.class);

        TypeElement output = asTypeElement(
                getMyValue(element, annotation,"outputType")
        );

        String methodName = annotation.methodName();


        List<TypeMirror> inputNodes = codeGenerator.findInputNodesWithType(element.asType(), NodeType.CUSTOM_PROCESS);

        // Assuming only two possible inputs. One Custom and one database
        TypeMirror target = inputNodes.get(0);

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
                javaFile.writeTo(processingEnv.getFiler());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
