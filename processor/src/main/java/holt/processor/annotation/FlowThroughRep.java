package holt.processor.annotation;

import com.squareup.javapoet.ClassName;
import holt.processor.DFDsProcessor;
import holt.processor.activator.FlowName;
import holt.processor.activator.Process;

import java.util.Arrays;
import java.util.List;

import static holt.processor.AnnotationValueUtils.getAnnotationClassValue;

public record FlowThroughRep(Process process,
                             FlowName flowName,
                             String functionName,
                             ClassName outputType,
                             List<QueryRep> queries) {

    public static FlowThroughRepBuilder of(Process process, FlowThrough flowThrough) {
        return processor -> new FlowThroughRep(
                process,
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
