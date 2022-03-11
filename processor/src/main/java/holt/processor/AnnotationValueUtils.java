package holt.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.function.Function;

public final class AnnotationValueUtils {

    private AnnotationValueUtils() {}

    public static <T extends Annotation> TypeElement getAnnotationClassValue(DFDsProcessor processor, T anno, Function<T, Class<?>> func) {
        ProcessingEnvironment processingEnvironment = processor.getProcessingEnvironment();
        Elements elements = processingEnvironment.getElementUtils();

        // TODO: Find a better way rather than try/catch
        TypeMirror typeMirror;
        try {
            typeMirror = elements.getTypeElement(func.apply(anno).getCanonicalName()).asType();
        } catch (MirroredTypeException e) {
            typeMirror = e.getTypeMirror();
        }

        return (TypeElement) processingEnvironment.getTypeUtils().asElement(typeMirror);
    }

}
