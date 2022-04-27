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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@FlowThrough(
        traverse = "CW",
        output = @Output(type = NotePolicy.class),
        functionName = "CW",
        queries = {
                @Query(
                        db = NotesDatabasePolicy.class,
                        output = @Output(type = NotePolicy.class, collection = true)
                )
        }
)
@Activator(instantiateWithReflection = true)
public class NotesDatabaseToWordCounterCWRequest implements NotesDatabaseToWordCounterCWRequestRequirements {


    @Override
    public Map<Note, NotePolicy> CW(Collection<Note> input0, Collection<NotePolicy> dbInput1) {
        HashMap<Note, NotePolicy> m = new HashMap<>();
        List<Note> a1 = new ArrayList<>(input0);
        List<NotePolicy> a2 = new ArrayList<>(dbInput1);
        for (int i = 0; i < a1.size(); i++) {
            m.put(a1.get(i), a2.get(i));
        }

        return m;
    }

    @Override
    public NotesDatabasePolicyToNotesDatabaseToWordCounterCWRequestCWQuery queryNotesDatabasePolicyCW(Collection<Note> input0) {
        return NotesDatabasePolicy::getPolicies;
    }
}
