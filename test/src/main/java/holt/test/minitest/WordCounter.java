package holt.test.minitest;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.annotation.Query;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCountWordsQuery;
import holt.processor.generation.minitest.WordCounterRequirements;
import holt.test.minitest.NotesDatabase;
import holt.test.minitest.data.CountWordsAction;
import holt.test.minitest.data.CountWordsActionPolicy;
import holt.test.minitest.data.Note;

import java.util.Collection;
import java.util.stream.Collectors;

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
    public Integer countWords(CountWordsAction input0, Collection<Note> input1) {
        return input1.stream().map(Note::text).collect(Collectors.joining()).split(" ").length;
    }

    @Override
    public NotesDatabaseToWordCounterCountWordsQuery queryNotesDatabaseCountWords(CountWordsAction input0) {
        return db -> null;
    }

}
