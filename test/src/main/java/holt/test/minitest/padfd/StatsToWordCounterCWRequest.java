package holt.test.minitest.padfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.StatsToWordCounterCWRequestRequirements;
import holt.test.minitest.data.CountWordsAction;
import holt.test.minitest.data.CountWordsActionPolicy;

import java.util.HashMap;
import java.util.Map;

@FlowThrough(
        traverse = "CW",
        functionName = "CW",
        output = @Output(type = CountWordsActionPolicy.class)
)
@Activator(instantiateWithReflection = true)
public class StatsToWordCounterCWRequest implements StatsToWordCounterCWRequestRequirements {
    @Override
    public Map<CountWordsAction, CountWordsActionPolicy> CW(CountWordsActionPolicy input0) {
        return new HashMap<>();
    }
}
