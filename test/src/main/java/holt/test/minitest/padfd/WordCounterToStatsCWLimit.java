package holt.test.minitest.padfd;

import holt.processor.generation.minitest.WordCounterToStatsCWLimitRequirements;
import holt.test.minitest.data.CountedWordsPolicy;

import java.util.Map;
import java.util.function.Predicate;

public class WordCounterToStatsCWLimit implements WordCounterToStatsCWLimitRequirements {
    @Override
    public Predicate<Integer> CW(Map<Integer, CountedWordsPolicy> input0) {
        return null;
    }
}
