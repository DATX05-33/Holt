package holt.processor.annotation;

import com.squareup.javapoet.ClassName;
import holt.processor.DFDsProcessor;
import holt.processor.activator.ExternalEntity;
import holt.processor.activator.FlowName;

import static holt.processor.AnnotationValueUtils.getAnnotationClassValue;

public record FlowStartRep(
        ExternalEntity externalEntity,
        FlowName flowName,
        ClassName flowStartType
) {

    public static FlowStartRepBuilder of(FlowStart flowStart, ExternalEntity externalEntity) {
        return processor -> new FlowStartRep(
                externalEntity,
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
