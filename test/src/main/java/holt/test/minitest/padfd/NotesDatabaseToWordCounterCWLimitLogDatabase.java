package holt.test.minitest.padfd;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCWLimitLog;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCWLimitLogDatabaseRequirements;

import java.util.Collection;

@Activator(instantiateWithReflection = true)
public class NotesDatabaseToWordCounterCWLimitLogDatabase implements NotesDatabaseToWordCounterCWLimitLogDatabaseRequirements {

    @Override
    public void CW(Collection<NotesDatabaseToWordCounterCWLimitLog.Row> logs) {
        logs.forEach(System.out::println);
    }
}
