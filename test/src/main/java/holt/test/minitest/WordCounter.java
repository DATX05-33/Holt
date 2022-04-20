package holt.test.minitest;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCountWordsQuery;
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
    public Integer countWords(CountWordsAction input0, Collection<Note> notes) {
        int n = 0;
        for (Note note : notes) {
            n += note.text().split(" ").length;
        }

        return n;
    }

    @Override
    public NotesDatabaseToWordCounterCountWordsQuery queryNotesDatabaseCountWords(CountWordsAction input0) {
        return NotesDatabase::getNotes;
    }
}
