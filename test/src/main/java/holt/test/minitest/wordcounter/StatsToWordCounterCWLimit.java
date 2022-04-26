package holt.test.minitest.wordcounter;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.StatsToWordCounterCWLimitRequirements;
import holt.test.minitest.data.CountWordsAction;
import holt.test.minitest.data.CountWordsActionPolicy;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NotePolicy;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

@Activator(instantiateWithReflection = true)
public class StatsToWordCounterCWLimit implements StatsToWordCounterCWLimitRequirements {
    @Override
    public Predicate<CountWordsActionPolicy> CW(Map<CountWordsAction, Object> input0) {
        return null;
    }
}
