package holt.processor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.function.Function;

public class AnnotationValueHelper {

    public static AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> clazz) {
        String clazzName = clazz.getName();
        for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            String a = m.getAnnotationType().toString();
            if (a.equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    /**
     * For Class attribute, if we invoke directly, it may throw {@link MirroredTypeException} because the class has not been
     * compiled. Use this method to get the Class value safely.
     *
     * @param elements Elements for convert Class to TypeMirror
     * @param anno annotation object
     * @param func the invocation of get Class value
     * @return the value's {@link TypeMirror}
     */
    public static <T extends Annotation> TypeMirror getAnnotationClassValue(Elements elements, T anno,
                                                                            Function<T, Class<?>> func) {
        try {
            return elements.getTypeElement(func.apply(anno).getCanonicalName()).asType();
        } catch (MirroredTypeException e) {
            return e.getTypeMirror();
        }
    }

    public static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static TypeMirror getMyValue(TypeElement foo, Annotation annotation, String key) {
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
