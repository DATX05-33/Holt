package holt.test.minitest.notesdatabase;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.annotation.Query;
import holt.processor.generation.minitest.NotesPolicyDatabaseToNotesDatabaseToWordCounterRequestCWQuery;
import holt.processor.generation.minitest.WordCounterRequestNotesRequirements;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NotePolicy;

import java.util.Collection;
import java.util.Map;

@FlowThrough(
        traverse = "CW",
        output = @Output(type = NotePolicy.class),
        functionName = "CW",
        queries = {
                @Query(
                        db = NotesPolicyDatabase.class,
                        output = @Output(type = NotePolicy.class, collection = true)
                )
        }
)
@Activator(instantiateWithReflection = true, graphName = "WordCounterRequestNotes")
public class NotesDatabaseToWordCounterRequest implements WordCounterRequestNotesRequirements {

    @Override
    public Map<Note, NotePolicy> CW(Collection<Note> input0, Collection<NotePolicy> dbInput1) {
        return null;
    }

    @Override
    public NotesPolicyDatabaseToNotesDatabaseToWordCounterRequestCWQuery queryNotesPolicyDatabaseCW(Collection<Note> input0) {
        return null;
    }
}
