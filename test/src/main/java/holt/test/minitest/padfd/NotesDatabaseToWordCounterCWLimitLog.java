package holt.test.minitest.padfd;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCWLimitLogRequirements;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NotePolicy;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

@Activator(instantiateWithReflection = true)
public class NotesDatabaseToWordCounterCWLimitLog implements NotesDatabaseToWordCounterCWLimitLogRequirements {
    @Override
    public Object CW(Predicate<Note> input0, Map<Note, NotePolicy> input1) {
        return null;
    }
}
