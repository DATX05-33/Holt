package holt.processor;

import holt.processor.annotation.FlowStart;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

public class AnnotationValueHelper {

    public static AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> clazz) {

        /*var f = typeElement.getAnnotationMirrors();

        var g = f.get(0).getElementValues();

        for (var h : g.values()) {

            var i = h.getValue().getClass();
            if (h instanceof List j) {
                var m = j.get(0);
                System.out.println("nfwlkan");
            }
            System.out.println("yoo");
        }*/

        String clazzName = clazz.getName();
        for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            String a = m.getAnnotationType().toString();
            if (a.equals(clazzName)) {
                return m;
            }
        }
        return null;
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
