package holt.test.minitest.stats_padfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.annotation.Query;
import holt.processor.generation.minitest.NotesDatabasePolicyToNotesDatabaseToWordCounterCWRequestCWQuery;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCWRequestRequirements;
import holt.test.minitest.NotesDatabasePolicy;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NotePolicy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@FlowThrough(
        traverse = "CW",
        output = @Output(type = NotePolicy.class),
        functionName = "CW",
        queries = {
                @Query(
                        db = NotesDatabasePolicy.class,
                        output = @Output(type = NotePolicy.class)
                )
        }
)
@Activator(instantiateWithReflection = true)
public class NotesDatabaseToWordCounterCWRequest implements NotesDatabaseToWordCounterCWRequestRequirements {

    @Override
    public Map<Note, NotePolicy> CW(Collection<Note> input0, NotePolicy dbInput1) {
        return new HashMap<>();
    }

    @Override
    public NotesDatabasePolicyToNotesDatabaseToWordCounterCWRequestCWQuery queryNotesDatabasePolicyCW(Collection<Note> input0) {
        return db -> null;
    }
}
