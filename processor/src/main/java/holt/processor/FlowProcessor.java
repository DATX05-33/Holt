package holt.processor;

import com.squareup.javapoet.JavaFile;
import holt.processor.annotation.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FlowProcessor {

    private final CodeGenerator codeGenerator = CodeGenerator.getInstance();

    private final List<JavaFile> saved = new ArrayList<>();

    private ProcessingEnvironment processingEnv;

    public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
        // first pass we map String to TypeMirrors for later use
        if (!env.processingOver()) {
            for (Element element : env.getElementsAnnotatedWith(FlowStarts.class)) {
                if (element instanceof TypeElement typeElement) {
                    // all SimpleNames have to be unique and same as the node value in the PADFD
                    addFlowStartsTypeMirror(typeElement);
                }
            }

            for (Element element : env.getElementsAnnotatedWith(FlowThroughs.class)) {
                if (element instanceof TypeElement typeElement) {
                    // all SimpleNames have to be unique and same as the node value in the PADFD
                    addFlowThroughsTypeMirror(typeElement);
                }
            }

//            for (Element element : env.getElementsAnnotatedWith(Database.class)) {
//                if (element instanceof TypeElement typeElement) {
//                    addDBTypeMirrors(typeElement);
//                }
//            }

            for (Element element : env.getElementsAnnotatedWith(FlowStarts.class)) {
                if (element instanceof TypeElement typeElement) {
                    // all SimpleNames have to be unique and same as the node value in the PADFD
                    flowStarts(typeElement);
                }

            }

            for (Element element : env.getElementsAnnotatedWith(FlowThroughs.class)) {
                if (element instanceof TypeElement typeElement) {
                    // all SimpleNames have to be unique and same as the node value in the PADFD
                    flowThroughs(typeElement);
                }
            }

            for (Element element : env.getElementsAnnotatedWith(FlowStart.class)) {
                if (element instanceof TypeElement typeElement) {
                    // all SimpleNames have to be unique and same as the node value in the PADFD
                    addStartTypeMirror(typeElement);
                }
            }

            for (Element element : env.getElementsAnnotatedWith(FlowThrough.class)) {
                if (element instanceof TypeElement typeElement) {
                    // all SimpleNames have to be unique and same as the node value in the PADFD
                    addThroughTypeMirror(typeElement);
                }
            }

            // then we map the outputs and inputs
            for (Element element : env.getElementsAnnotatedWith(FlowStart.class)) {
                if (element instanceof TypeElement typeElement) {
                    FlowStart annotation = typeElement.getAnnotation(FlowStart.class);
                    mapInputOutputStart(typeElement, annotation);
                }
            }

            // then we map the outputs and inputs
            for (Element element : env.getElementsAnnotatedWith(FlowThrough.class)) {
                if (element instanceof TypeElement typeElement) {
                    FlowThrough annotation = typeElement.getAnnotation(FlowThrough.class);
                    mapInputOutputThrough(typeElement, annotation);
                }
            }

            // and lastly generate the interfaces
            List<JavaFile> interfaces = codeGenerator.generateInterfaces();

            for (JavaFile j : interfaces) {
//                saveJavaFile(j);
            }
        }

        return true;
    }

    private void addFlowThroughsTypeMirror(TypeElement typeElement) {
        FlowThroughs annotation = typeElement.getAnnotation(FlowThroughs.class);

        FlowThrough[] a = annotation.value();

        for (FlowThrough f : a) {
            TypeMirror output = AnnotationValueHelper.getAnnotationClassValue(processingEnv.getElementUtils(), f, FlowThrough::outputType);
            String outputName = output.toString().substring(output.toString().lastIndexOf('.') + 1);

            codeGenerator.addTypeMirror(typeElement.getSimpleName().toString(), typeElement.asType());
            codeGenerator.addTypeMirror(outputName, typeElement.asType());
        }
    }

    private void addFlowStartsTypeMirror(TypeElement typeElement) {
        FlowStarts annotation = typeElement.getAnnotation(FlowStarts.class);

        FlowStart[] a = annotation.value();

        for (FlowStart f : a) {
            TypeMirror output = AnnotationValueHelper.getAnnotationClassValue(processingEnv.getElementUtils(), f, FlowStart::flowStartType);
            String outputName = output.toString().substring(output.toString().lastIndexOf('.') + 1);

            codeGenerator.addTypeMirror(typeElement.getSimpleName().toString(), typeElement.asType());
            codeGenerator.addTypeMirror(outputName, output);
        }
    }

    private void flowThroughs(TypeElement typeElement) {
        FlowThroughs annotation = typeElement.getAnnotation(FlowThroughs.class);

        FlowThrough[] a = annotation.value();

        for (FlowThrough f : a) {
            TypeMirror output = AnnotationValueHelper.getAnnotationClassValue(processingEnv.getElementUtils(), f, FlowThrough::outputType);

            String flow = f.flow();
            String functionName = f.functionName();

            TypeMirror target = codeGenerator.findTarget(flow, typeElement);

            codeGenerator.addOutputTypeAndFunctionName(flow, typeElement.asType(), output, target, functionName);
        }
    }

    private void flowStarts(TypeElement typeElement) {
        FlowStarts annotation = typeElement.getAnnotation(FlowStarts.class);

        FlowStart[] a = annotation.value();
        for (FlowStart f : a) {
            TypeMirror output = AnnotationValueHelper.getAnnotationClassValue(processingEnv.getElementUtils(), f, FlowStart::flowStartType);

            String flow = f.flow();

            TypeMirror target = codeGenerator.findTarget(flow, typeElement);

            codeGenerator.addOutputType(flow, typeElement.asType(), output, target);

            codeGenerator.addOutputTypeAndFunctionName(flow, typeElement.asType(), output, target, flow);
        }
    }

    private void mapInputOutputThrough(TypeElement typeElement, FlowThrough annotation) {
        TypeElement output = asTypeElement(
                AnnotationValueHelper.getMyValue(typeElement, annotation,"outputType")
        );

        String flow = annotation.flow();
        String functionName = annotation.functionName();

        TypeMirror target = codeGenerator.findTarget(flow, typeElement);

        codeGenerator.addOutputTypeAndFunctionName(flow, typeElement.asType(), output.asType(), target, functionName);
    }

    private void mapInputOutputStart(TypeElement typeElement, FlowStart annotation)  {
        TypeMirror a = AnnotationValueHelper.getMyValue(typeElement, annotation,"flowStartType");
        TypeElement output = asTypeElement(a);


        String flow = annotation.flow();

        TypeMirror target = codeGenerator.findTarget(flow, typeElement);

        codeGenerator.addOutputType(flow, typeElement.asType(), output.asType(), target);
    }

    private void addDBTypeMirrors(TypeElement typeElement) {
        codeGenerator.addTypeMirror(typeElement.getSimpleName().toString(), typeElement.asType());
    }

    private void addThroughTypeMirror(TypeElement typeElement) {
        FlowThrough annotation = typeElement.getAnnotation(FlowThrough.class);

        TypeElement output = asTypeElement(
                AnnotationValueHelper.getMyValue(typeElement, annotation,"outputType")
        );

        codeGenerator.addTypeMirror(typeElement.getSimpleName().toString(), typeElement.asType());
        codeGenerator.addTypeMirror(output.getSimpleName().toString(), output.asType());
    }

    private void addStartTypeMirror(TypeElement typeElement) {
        FlowStart annotation = typeElement.getAnnotation(FlowStart.class);

        TypeElement output = asTypeElement(
                AnnotationValueHelper.getMyValue(typeElement, annotation,"flowStartType")
        );

        codeGenerator.addTypeMirror(typeElement.getSimpleName().toString(), typeElement.asType());
        codeGenerator.addTypeMirror(output.getSimpleName().toString(), output.asType());
    }

    private TypeElement asTypeElement(TypeMirror typeMirror) {
        Types TypeUtils = this.processingEnv.getTypeUtils();
        return (TypeElement) TypeUtils.asElement(typeMirror);
    }

}
