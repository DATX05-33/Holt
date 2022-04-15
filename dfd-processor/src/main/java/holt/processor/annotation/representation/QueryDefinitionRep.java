package holt.processor.annotation.representation;

import com.squareup.javapoet.ClassName;
import holt.processor.DFDsProcessor;
import holt.processor.activator.DatabaseActivatorAggregate;
import holt.processor.activator.ProcessActivatorAggregate;
import holt.processor.annotation.QueryDefinition;

import static holt.processor.AnnotationValueUtils.getAnnotationClassValue;

public record QueryDefinitionRep(DatabaseActivatorAggregate db, ProcessActivatorAggregate process, ClassName type) {

    public static QueryDefinitionRepBuilder of(QueryDefinition queryDefinition) {
        return (processor, convertersResult) -> {
            ClassName processClassName = ClassName.bestGuess(getAnnotationClassValue(
                    processor, queryDefinition, QueryDefinition::process
            ).toString());
            ClassName dbClassName = ClassName.bestGuess(getAnnotationClassValue(
                    processor, queryDefinition, QueryDefinition::db
            ).toString());

            ProcessActivatorAggregate processActivatorAggregate = (ProcessActivatorAggregate) convertersResult.getActivatorAggregateByClassName(processClassName);
            DatabaseActivatorAggregate databaseActivatorAggregate = (DatabaseActivatorAggregate) convertersResult.getActivatorAggregateByClassName(dbClassName);

            return new QueryDefinitionRep(
                    databaseActivatorAggregate,
                    processActivatorAggregate,
                    ClassName.bestGuess(getAnnotationClassValue(
                            processor, queryDefinition, QueryDefinition::type
                    ).toString())
            );
        };
    }

    public interface QueryDefinitionRepBuilder {
        QueryDefinitionRep with(DFDsProcessor processor, DFDsProcessor.ConvertersResult convertersResult);
    }

}
