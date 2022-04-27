package holt.test.minitest.notes_padfd.dn;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NoteFormatterToNotesDatabaseDNRequestRequirements;

import java.util.Map;

@Activator(instantiateWithReflection = true)
public class NoteFormatterToNotesDatabaseDNRequest implements NoteFormatterToNotesDatabaseDNRequestRequirements {
    @Override
    public Map<Object, Object> DN(Object input0) {
        return null;
    }
}
