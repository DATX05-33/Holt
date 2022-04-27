package holt.test.minitest.stats_padfd;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.WordCounterToStatsCWLimitLog;
import holt.processor.generation.minitest.WordCounterToStatsCWLimitLogDatabaseRequirements;

@Activator(instantiateWithReflection = true)
public class WordCounterToStatsCWLimitLogDatabase implements WordCounterToStatsCWLimitLogDatabaseRequirements {
    @Override
    public void CW(WordCounterToStatsCWLimitLog.Row log) {
        System.out.println(log);
    }
}
