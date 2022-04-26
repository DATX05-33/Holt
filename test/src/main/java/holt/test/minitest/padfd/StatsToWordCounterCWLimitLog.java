package holt.test.minitest.padfd;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.StatsToWordCounterCWLimitLogRequirements;
import holt.test.minitest.data.CountWordsAction;
import holt.test.minitest.data.CountWordsActionPolicy;

import java.util.Map;
import java.util.function.Predicate;

@Activator(instantiateWithReflection = true)
public class StatsToWordCounterCWLimitLog implements StatsToWordCounterCWLimitLogRequirements {
    @Override
    public Object CW(Predicate<CountWordsAction> input0, Map<CountWordsAction, CountWordsActionPolicy> input1, CountWordsAction input2) {
        return null;
    }
}
