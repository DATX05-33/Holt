package holt.test.minitest.notes_padfd.an;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.NoteFormatterToNotesDatabaseANLimitLog;
import holt.processor.generation.minitest.NoteFormatterToNotesDatabaseANLimitLogDatabaseRequirements;

@Activator(instantiateWithReflection = true)
public class NoteFormatterToNotesDatabaseANLimitLogDatabase implements NoteFormatterToNotesDatabaseANLimitLogDatabaseRequirements {
    @Override
    public void AN(NoteFormatterToNotesDatabaseANLimitLog.Row input0) {

    }
}
