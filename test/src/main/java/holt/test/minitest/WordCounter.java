package holt.test.minitest;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.minitest.WordCounterRequirements;

import java.util.Collection;

@FlowThrough(
        traverse = "CW",
        functionName = "countWords",
        outputType = Integer.class,
        queries = {
                @Query(
                        db = NotesDatabase.class,
                        type = Note.class,
                        isCollection = true
                )
        }
)
@Activator(instantiateWithReflection = true)
public class WordCounter implements WordCounterRequirements {


    @Override
    public Integer countWords(Object input0, Object input1) {
        return null;
    }
}
