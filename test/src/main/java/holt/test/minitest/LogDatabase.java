package holt.test.minitest;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NoteFormatterToNotesDatabaseANLimitLog;
import holt.processor.generation.minitest.NoteFormatterToNotesDatabaseANLimitLogDatabaseRequirements;
import holt.processor.generation.minitest.NoteFormatterToNotesDatabaseDNLimitLog;
import holt.processor.generation.minitest.NoteFormatterToNotesDatabaseDNLimitLogDatabaseRequirements;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCWLimitLog;
import holt.processor.generation.minitest.NotesDatabaseToWordCounterCWLimitLogDatabaseRequirements;
import holt.processor.generation.minitest.NotesToNoteFormatterANLimitLog;
import holt.processor.generation.minitest.NotesToNoteFormatterANLimitLogDatabaseRequirements;
import holt.processor.generation.minitest.NotesToNoteFormatterDNLimitLog;
import holt.processor.generation.minitest.NotesToNoteFormatterDNLimitLogDatabaseRequirements;
import holt.processor.generation.minitest.StatsToWordCounterCWLimitLog;
import holt.processor.generation.minitest.StatsToWordCounterCWLimitLogDatabaseRequirements;
import holt.processor.generation.minitest.WordCounterToStatsCWLimitLog;
import holt.processor.generation.minitest.WordCounterToStatsCWLimitLogDatabaseRequirements;

import java.util.Collection;

@Activator(instantiateWithReflection = true)
public class LogDatabase implements
        NoteFormatterToNotesDatabaseANLimitLogDatabaseRequirements,
        NotesToNoteFormatterANLimitLogDatabaseRequirements,
        NoteFormatterToNotesDatabaseDNLimitLogDatabaseRequirements,
        NotesToNoteFormatterDNLimitLogDatabaseRequirements,
        NotesDatabaseToWordCounterCWLimitLogDatabaseRequirements,
        StatsToWordCounterCWLimitLogDatabaseRequirements,
        WordCounterToStatsCWLimitLogDatabaseRequirements {
    @Override
    public void AN(NoteFormatterToNotesDatabaseANLimitLog.Row input0) {
        System.out.println(input0);
    }

    @Override
    public void DN(NoteFormatterToNotesDatabaseDNLimitLog.Row input0) {
        System.out.println(input0);
    }

    @Override
    public void CW(Collection<NotesDatabaseToWordCounterCWLimitLog.Row> input0) {
        for (NotesDatabaseToWordCounterCWLimitLog.Row row : input0) {
            System.out.println(row);
        }
    }

    @Override
    public void AN(NotesToNoteFormatterANLimitLog.Row input0) {
        System.out.println(input0);
    }

    @Override
    public void DN(NotesToNoteFormatterDNLimitLog.Row input0) {
        System.out.println(input0);
    }

    @Override
    public void CW(StatsToWordCounterCWLimitLog.Row input0) {
        System.out.println(input0);
    }

    @Override
    public void CW(WordCounterToStatsCWLimitLog.Row input0) {
        System.out.println(input0);
    }
}
