package holt.test.minitest.padfd;

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
    public Predicate<CountWordsAction> CW(Map<CountWordsAction, CountWordsActionPolicy> input0) {
        return countWordsAction -> true;
    }
}
