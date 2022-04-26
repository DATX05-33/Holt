package holt.test.minitest.wordcounter;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.annotation.Query;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCountWordsQuery;
import holt.processor.generation.minitest.WordCounterRequirements;
import holt.test.minitest.NotesDatabase;
import holt.test.minitest.data.CountWordsActionPolicy;
import holt.test.minitest.data.Note;

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
