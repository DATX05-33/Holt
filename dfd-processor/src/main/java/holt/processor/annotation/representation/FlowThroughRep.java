package holt.processor.annotation.representation;

import com.squareup.javapoet.ClassName;
import holt.processor.DFDsProcessor;
import holt.processor.activator.ProcessActivatorAggregate;
import holt.processor.activator.TraverseName;
import holt.processor.annotation.FlowThrough;

import java.util.Arrays;
import java.util.List;

import static holt.processor.AnnotationValueUtils.getAnnotationClassValue;

public record FlowThroughRep(ProcessActivatorAggregate processActivator,
                             TraverseName traverseName,
                             String functionName,
                             ClassName outputType,
                             List<QueryRep> queries,
                             List<QueryDefinitionRep> overrideQueries) {

    public static FlowThroughRepBuilder of(ProcessActivatorAggregate processActivator, DFDsProcessor.ConvertersResult convertersResult, FlowThrough flowThrough) {
        return processor -> new FlowThroughRep(
                processActivator,
                new TraverseName(flowThrough.traverse()),
                flowThrough.functionName(),
                ClassName.bestGuess(getAnnotationClassValue(
                        processor, flowThrough, FlowThrough::outputType
                ).toString()),
                Arrays.stream(flowThrough.queries())
                        .map(query -> QueryRep.of(query).with(processor))
                        .toList(),
                Arrays.stream(flowThrough.overrideQueries())
                        .map(queryDefinition -> QueryDefinitionRep.of(queryDefinition).with(processor, convertersResult))
                        .toList()
        );
    }

    public interface FlowThroughRepBuilder {
        FlowThroughRep with(DFDsProcessor processor);
    }

}
