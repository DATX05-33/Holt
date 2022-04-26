package holt.test.minitest.padfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.WordCounterToStatsCWRequestRequirements;
import holt.test.minitest.data.CountedWordsPolicy;

import java.util.HashMap;
import java.util.Map;

@FlowThrough(
        traverse = "CW",
        output = @Output(type = CountedWordsPolicy.class),
        functionName = "CW"
)
@Activator(instantiateWithReflection = true)
public class WordCounterToStatsCWRequest implements WordCounterToStatsCWRequestRequirements {
    @Override
    public Map<Integer, CountedWordsPolicy> CW(CountedWordsPolicy input0) {
        return null;
    }
}
