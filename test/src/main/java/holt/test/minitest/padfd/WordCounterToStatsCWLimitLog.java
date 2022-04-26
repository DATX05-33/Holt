package holt.test.minitest.padfd;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.WordCounterToStatsCWLimitLogRequirements;
import holt.test.minitest.data.CountedWordsPolicy;

import java.util.Map;
import java.util.function.Predicate;

@Activator(instantiateWithReflection = true)
public class WordCounterToStatsCWLimitLog implements WordCounterToStatsCWLimitLogRequirements {
    @Override
    public Object CW(Predicate<Integer> input0, Map<Integer, CountedWordsPolicy> input1, Integer input2) {
        return null;
    }
}
