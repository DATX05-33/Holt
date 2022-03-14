package holt.processor.annotation.representation;

import com.squareup.javapoet.ClassName;
import holt.processor.DFDsProcessor;
import holt.processor.activator.FlowName;
import holt.processor.activator.ProcessActivator;
import holt.processor.annotation.FlowThrough;

import java.util.Arrays;
import java.util.List;

import static holt.processor.AnnotationValueUtils.getAnnotationClassValue;

public record FlowThroughRep(ProcessActivator processActivator,
                             FlowName flowName,
                             String functionName,
                             ClassName outputType,
                             List<QueryRep> queries) {

    public static FlowThroughRepBuilder of(ProcessActivator processActivator, FlowThrough flowThrough) {
        return processor -> new FlowThroughRep(
                processActivator,
                new FlowName(flowThrough.flow()),
                flowThrough.functionName(),
                ClassName.bestGuess(getAnnotationClassValue(
                        processor, flowThrough, FlowThrough::outputType
                ).toString()),
                Arrays.stream(flowThrough.queries())
                        .map(query -> QueryRep.of(query).with(processor))
                        .toList()
        );
    }

    public interface FlowThroughRepBuilder {
        FlowThroughRep with(DFDsProcessor processor);
    }

}
