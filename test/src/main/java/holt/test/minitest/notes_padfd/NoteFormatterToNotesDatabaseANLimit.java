package holt.test.minitest.notes_padfd;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NoteFormatterToNotesDatabaseANLimitRequirements;
import holt.test.minitest.data.NoteInsertion;
import holt.test.minitest.data.NotePolicy;

import java.util.Map;
import java.util.function.Predicate;

@Activator(instantiateWithReflection = true)
public class NoteFormatterToNotesDatabaseANLimit implements NoteFormatterToNotesDatabaseANLimitRequirements {
    @Override
    public Predicate<NoteInsertion> AN(Map<NoteInsertion, NotePolicy> input0) {
        return noteInsertion -> true;
    }
}
