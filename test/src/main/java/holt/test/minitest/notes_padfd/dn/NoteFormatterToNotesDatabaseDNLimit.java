package holt.test.minitest.notes_padfd.dn;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.NoteFormatterToNotesDatabaseDNLimitRequirements;
import holt.test.minitest.data.NotePolicy;

import java.util.Map;
import java.util.function.Predicate;

@Activator(instantiateWithReflection = true)
public class NoteFormatterToNotesDatabaseDNLimit implements NoteFormatterToNotesDatabaseDNLimitRequirements {

    @Override
    public Predicate<Object> DN(Map<Object, NotePolicy> input0) {
        return null;
    }
}
