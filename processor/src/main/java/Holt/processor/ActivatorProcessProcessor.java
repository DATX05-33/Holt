package Holt.processor;

import Holt.processor.annotation.ActivatorProcess;
import Holt.processor.generation.CodeGeneration;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
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
     *
     * https://stackoverflow.com/questions/7687829/java-6-annotation-processing-getting-a-class-from-an-annotation/10167558#10167558
     *
     * @param typeElement class
     */
    private void activatorProcess(Element typeElement) throws ClassNotFoundException {
        ActivatorProcess a = typeElement.getAnnotation(ActivatorProcess.class);

        TypeElement input = asTypeElement(getInput(a));
        TypeElement output = asTypeElement(getOutput(a));

        JavaFile javaFile = CodeGeneration.genereateMethodFromAnnotation(
                typeElement,
                Class.forName(String.valueOf(input.getQualifiedName())),
                Class.forName(String.valueOf(output.getQualifiedName())),
                a.methodName()
        );

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypeElement asTypeElement(TypeMirror typeMirror) {
        Types TypeUtils = this.processingEnv.getTypeUtils();
        return (TypeElement)TypeUtils.asElement(typeMirror);
    }

    private static TypeMirror getInput(ActivatorProcess annotation) {
        try
        {
            annotation.input(); // this should throw
        }
        catch( MirroredTypeException mte )
        {
            return mte.getTypeMirror();
        }
        return null; // can this ever happen ??
    }

    private static TypeMirror getOutput(ActivatorProcess annotation) {
        try
        {
            annotation.output(); // this should throw
        }
        catch( MirroredTypeException mte )
        {
            return mte.getTypeMirror();
        }
        return null; // can this ever happen ??
    }

    private static TypeMirror getMethodName(ActivatorProcess annotation) {
        try
        {
            annotation.methodName(); // this should throw
        }
        catch( MirroredTypeException mte )
        {
            return mte.getTypeMirror();
        }
        return null; // can this ever happen ??
    }
}
