package holt.test.minitest;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesDatabasePolicyRequirements;
import holt.test.minitest.data.NoteInsertion;
import holt.test.minitest.data.NotePolicy;

import java.util.Map;

@Activator(instantiateWithReflection = true)
public class NotesDatabasePolicy implements NotesDatabasePolicyRequirements {


    @Override
    public void DN(Map<Object, NotePolicy> input0) {

    }

    @Override
    public void AN(Map<NoteInsertion, NotePolicy> input0) {

    }
}
