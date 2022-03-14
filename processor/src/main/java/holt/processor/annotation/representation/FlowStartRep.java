package holt.processor.annotation.representation;

import com.squareup.javapoet.ClassName;
import holt.processor.DFDsProcessor;
import holt.processor.activator.ExternalEntityActivator;
import holt.processor.activator.FlowName;
import holt.processor.annotation.FlowStart;

import static holt.processor.AnnotationValueUtils.getAnnotationClassValue;

public record FlowStartRep(
        ExternalEntityActivator externalEntityActivator,
        FlowName flowName,
        ClassName flowStartType
) {

    public static FlowStartRepBuilder of(FlowStart flowStart, ExternalEntityActivator externalEntityActivator) {
        return processor -> new FlowStartRep(
                externalEntityActivator,
                new FlowName(flowStart.flow()),
                ClassName.bestGuess(getAnnotationClassValue(
                        processor, flowStart, FlowStart::flowStartType
                ).toString())
        );
    }

    public interface FlowStartRepBuilder {
        FlowStartRep with(DFDsProcessor processor);
    }

}
