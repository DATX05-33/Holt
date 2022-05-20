package holt.test.minitest;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesDatabasePolicyRequirements;
import holt.test.minitest.data.NoteInsertion;
import holt.test.minitest.data.NotePolicy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static holt.test.minitest.NotesDatabase.n1;
import static holt.test.minitest.NotesDatabase.n2;

@Activator(instantiateWithReflection = true)
public class NotesDatabasePolicy implements NotesDatabasePolicyRequirements {

    private static final Map<UUID, NotePolicy> idToPolicy = new HashMap<>();

    static {
        idToPolicy.put(n1, new NotePolicy(Collections.EMPTY_LIST));
        idToPolicy.put(n2, new NotePolicy(List.of("count")));
    }

    @Override
    public void DN(Map<UUID, NotePolicy> input0) {
        
    }

    @Override
    public void AN(Map<NoteInsertion, NotePolicy> input0) {
        for (Map.Entry<NoteInsertion, NotePolicy> noteInsertionNotePolicyEntry : input0.entrySet()) {
            idToPolicy.put(noteInsertionNotePolicyEntry.getKey().note().id(), noteInsertionNotePolicyEntry.getValue());
        }
    }

    public List<NotePolicy> getPolicies() {
        return new ArrayList<>(idToPolicy.values());
    }

}
