package holt.processor.annotation.representation;

import com.squareup.javapoet.ClassName;
import holt.processor.DFDsProcessor;
import holt.processor.activator.ExternalEntityActivatorAggregate;
import holt.processor.activator.TraverseName;
import holt.processor.annotation.Traverse;

import static holt.processor.AnnotationValueUtils.getAnnotationClassValue;

public record TraverseRep(
        TraverseName name,
        ClassName flowStartType,
        String[] dataflows,
        ExternalEntityActivatorAggregate externalEntityActivator) {

    public static TraverseRep.TraverseRepBuilder of(Traverse traverse, ExternalEntityActivatorAggregate externalEntityActivator) {
        return processor -> new TraverseRep(
                new TraverseName(traverse.name()),
                ClassName.bestGuess(getAnnotationClassValue(
                        processor, traverse, Traverse::flowStartType
                ).toString()),
                traverse.order(),
                externalEntityActivator
        );
    }

    public interface TraverseRepBuilder {
        TraverseRep with(DFDsProcessor processor);
    }
}
