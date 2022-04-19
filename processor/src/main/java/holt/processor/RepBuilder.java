package holt.processor;

import holt.activator.DatabaseActivatorAggregate;
import holt.activator.ExternalEntityActivatorAggregate;
import holt.activator.ProcessActivatorAggregate;
import holt.activator.QualifiedName;
import holt.activator.TraverseName;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.annotation.QueryDefinition;
import holt.processor.annotation.Traverse;
import holt.applier.FlowThroughRep;
import holt.applier.QueryDefinitionRep;
import holt.applier.QueryRep;
import holt.applier.TraverseRep;

import java.util.Arrays;

public final class RepBuilder {

    private RepBuilder() { }

    public static TraverseRep createTraverseRep(Traverse traverse, ExternalEntityActivatorAggregate externalEntityActivator, DFDsProcessor processor) {
        return new TraverseRep(
                new TraverseName(traverse.name()),
                new QualifiedName(AnnotationValueUtils.getAnnotationClassValue(
                        processor, traverse, Traverse::flowStartType
                ).toString()),
                traverse.order(),
                externalEntityActivator
        );
    }

    public static FlowThroughRep createFlowThroughRep(ProcessActivatorAggregate processActivator, DFDsProcessor.ProcessorResults processorResults, FlowThrough flowThrough, DFDsProcessor processor) {
        return new FlowThroughRep(
                processActivator,
                new TraverseName(flowThrough.traverse()),
                flowThrough.functionName(),
                new QualifiedName(AnnotationValueUtils.getAnnotationClassValue(
                        processor, flowThrough, FlowThrough::outputType
                ).toString()),
                Arrays.stream(flowThrough.queries())
                        .map(query -> createQueryRep(query, processor))
                        .toList(),
                Arrays.stream(flowThrough.overrideQueries())
                        .map(queryDefinition -> createQueryDefinitionRep(queryDefinition, processorResults, processor))
                        .toList()
        );
    }

    public static QueryRep createQueryRep(Query query, DFDsProcessor processor) {
        return new QueryRep(
                new QualifiedName(AnnotationValueUtils.getAnnotationClassValue(
                        processor, query, Query::db
                ).toString()),
                new QualifiedName(AnnotationValueUtils.getAnnotationClassValue(
                        processor, query, Query::type
                ).toString())
        );
    }

    public static QueryDefinitionRep createQueryDefinitionRep(QueryDefinition queryDefinition, DFDsProcessor.ProcessorResults processorResults, DFDsProcessor processor) {
        QualifiedName processClassName = new QualifiedName(AnnotationValueUtils.getAnnotationClassValue(
                processor, queryDefinition, QueryDefinition::process
        ).toString());
        QualifiedName dbClassName = new QualifiedName(AnnotationValueUtils.getAnnotationClassValue(
                processor, queryDefinition, QueryDefinition::db
        ).toString());

        ProcessActivatorAggregate processActivatorAggregate = (ProcessActivatorAggregate) processorResults.getActivatorAggregateByClassName(processClassName);
        DatabaseActivatorAggregate databaseActivatorAggregate = (DatabaseActivatorAggregate) processorResults.getActivatorAggregateByClassName(dbClassName);

        return new QueryDefinitionRep(
                databaseActivatorAggregate,
                processActivatorAggregate,
                new QualifiedName(AnnotationValueUtils.getAnnotationClassValue(
                        processor, queryDefinition, QueryDefinition::type
                ).toString())
        );
    }

}
