package holt.test.minitest.stats_padfd;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCWLimitRequirements;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NotePolicy;

import java.util.Map;
import java.util.function.Predicate;

@Activator(instantiateWithReflection = true)
public class NotesDatabaseToWordCounterCWLimit implements NotesDatabaseToWordCounterCWLimitRequirements {
    @Override
    public Predicate<Note> CW(Map<Note, NotePolicy> input0) {
        return note -> input0.get(note).policies().contains("count");
    }
}
