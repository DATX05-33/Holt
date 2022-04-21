package holt.test.minitest;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.annotation.Query;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCountWordsQuery;
import holt.processor.generation.minitest.WordCounterRequirements;

@FlowThrough(
        traverse = "CW",
        functionName = "countWords",
        output = @Output(type = Integer.class),
        queries = {
                @Query(
                        db = NotesDatabase.class,
                        output = @Output(type = Note.class, collection = true)
                )
        }
)
@Activator(instantiateWithReflection = true)
public class WordCounter implements WordCounterRequirements {

    @Override
    public Integer countWords(Object input0, Object input1) {
        return null;
    }

    @Override
    public NotesDatabaseToWordCounterCountWordsQuery queryNotesDatabaseCountWords(Object input0, Object input1) {
        return null;
    }
}
