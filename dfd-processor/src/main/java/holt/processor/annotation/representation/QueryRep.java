package holt.processor.annotation.representation;

import com.squareup.javapoet.ClassName;
import holt.processor.DFDsProcessor;
import holt.processor.annotation.Query;

import static holt.processor.AnnotationValueUtils.getAnnotationClassValue;

public record QueryRep(ClassName db, ClassName type) {

    public static QueryRepBuilder of(Query query) {
        return processor -> new QueryRep(
                ClassName.bestGuess(getAnnotationClassValue(
                        processor, query, Query::db
                ).toString()),
                ClassName.bestGuess(getAnnotationClassValue(
                        processor, query, Query::type
                ).toString())
        );
    }

    public interface QueryRepBuilder {
        QueryRep with(DFDsProcessor processor);
    }

}
