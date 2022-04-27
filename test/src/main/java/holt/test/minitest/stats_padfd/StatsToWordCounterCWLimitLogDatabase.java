package holt.test.minitest.stats_padfd;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.StatsToWordCounterCWLimitLog;
import holt.processor.generation.minitest.StatsToWordCounterCWLimitLogDatabaseRequirements;

@Activator(instantiateWithReflection = true)
public class StatsToWordCounterCWLimitLogDatabase implements StatsToWordCounterCWLimitLogDatabaseRequirements {

    @Override
    public void CW(StatsToWordCounterCWLimitLog.Row log) {
        System.out.println(log);
    }
}
