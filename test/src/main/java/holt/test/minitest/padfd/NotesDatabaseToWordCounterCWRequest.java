package holt.test.minitest.padfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.annotation.Query;
import holt.processor.generation.minitest.NotesDatabasePolicyToNotesDatabaseToWordCounterCWRequestCWQuery;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCWRequestRequirements;
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
@Activator(instantiateWithReflection = true)
public class NotesDatabaseToWordCounterCWRequest implements NotesDatabaseToWordCounterCWRequestRequirements {

    @Override
    public Map<Note, NotePolicy> CW(Collection<Note> input0, Object dbInput1) {
        return null;
    }

    @Override
    public NotesDatabasePolicyToNotesDatabaseToWordCounterCWRequestCWQuery queryNotesDatabasePolicyCW(Collection<Note> input0) {
        return null;
    }
}
