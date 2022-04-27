package holt.test.minitest.stats_padfd;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.StatsToWordCounterCWLimitRequirements;
import holt.test.minitest.data.CountWordsAction;
import holt.test.minitest.data.CountWordsActionPolicy;

import java.util.Map;
import java.util.function.Predicate;

@Activator(instantiateWithReflection = true)
public class StatsToWordCounterCWLimit implements StatsToWordCounterCWLimitRequirements {
    @Override
    public Predicate<CountWordsAction> CW(Map<CountWordsAction, CountWordsActionPolicy> input0) {
        return countWordsAction -> true;
    }
}
