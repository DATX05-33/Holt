package holt.test.minitest.wordcounter;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.WordCounterRequestCountWordActionRequirements;
import holt.test.minitest.data.CountWordsAction;
import holt.test.minitest.data.CountWordsActionPolicy;

import java.util.Map;

@FlowThrough(traverse = "CW", output = @Output(type = CountWordsActionPolicy.class), functionName = "CW")
@Activator(instantiateWithReflection = true, graphName = "WordCounterRequestCountWordAction")
public class StatsToWordCounterRequest implements WordCounterRequestCountWordActionRequirements {

    @Override
    public Map<CountWordsAction, CountWordsActionPolicy> CW(CountWordsActionPolicy input0) {
        return null;
    }
}