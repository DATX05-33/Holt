package holt.processor;

import holt.DatabaseActivatorAggregate;
import holt.ExternalEntityActivatorAggregate;
import holt.ProcessActivatorAggregate;
import holt.QualifiedName;
import holt.TraverseName;
import holt.processor.AnnotationValueUtils;
import holt.processor.DFDsProcessor;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.annotation.QueryDefinition;
import holt.processor.annotation.Traverse;
import holt.representation.FlowThroughRep;
import holt.representation.QueryDefinitionRep;
import holt.representation.QueryRep;
import holt.representation.TraverseRep;

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

    public static FlowThroughRep createFlowThroughRep(ProcessActivatorAggregate processActivator, DFDsProcessor.ConvertersResult convertersResult, FlowThrough flowThrough, DFDsProcessor processor) {
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
                        .map(queryDefinition -> createQueryDefinitionRep(queryDefinition, convertersResult, processor))
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

    public static QueryDefinitionRep createQueryDefinitionRep(QueryDefinition queryDefinition, DFDsProcessor.ConvertersResult convertersResult, DFDsProcessor processor) {
        QualifiedName processClassName = new QualifiedName(AnnotationValueUtils.getAnnotationClassValue(
                processor, queryDefinition, QueryDefinition::process
        ).toString());
        QualifiedName dbClassName = new QualifiedName(AnnotationValueUtils.getAnnotationClassValue(
                processor, queryDefinition, QueryDefinition::db
        ).toString());

        ProcessActivatorAggregate processActivatorAggregate = (ProcessActivatorAggregate) convertersResult.getActivatorAggregateByClassName(processClassName);
        DatabaseActivatorAggregate databaseActivatorAggregate = (DatabaseActivatorAggregate) convertersResult.getActivatorAggregateByClassName(dbClassName);

        return new QueryDefinitionRep(
                databaseActivatorAggregate,
                processActivatorAggregate,
                new QualifiedName(AnnotationValueUtils.getAnnotationClassValue(
                        processor, queryDefinition, QueryDefinition::type
                ).toString())
        );
    }

}
