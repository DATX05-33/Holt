package holt.test.minitest.notes_padfd.dn;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NoteFormatterToNotesDatabaseDNLimitRequirements;

import java.util.Map;
import java.util.function.Predicate;

@Activator(instantiateWithReflection = true)
public class NoteFormatterToNotesDatabaseDNLimit implements NoteFormatterToNotesDatabaseDNLimitRequirements {
    @Override
    public Predicate<Object> DN(Map<Object, Object> input0) {
        return null;
    }
}
