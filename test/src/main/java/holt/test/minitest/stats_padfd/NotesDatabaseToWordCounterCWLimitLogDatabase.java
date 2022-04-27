package holt.test.minitest.stats_padfd;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCWLimitLog;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCWLimitLogDatabaseRequirements;

import java.util.Collection;

@Activator(instantiateWithReflection = true)
public class NotesDatabaseToWordCounterCWLimitLogDatabase implements NotesDatabaseToWordCounterCWLimitLogDatabaseRequirements {

    @Override
    public void CW(Collection<NotesDatabaseToWordCounterCWLimitLog.Row> input0) {

    }
}
