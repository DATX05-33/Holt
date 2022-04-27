package holt.test.minitest.notes_padfd.dn;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesToNoteFormatterDNRequestRequirements;
import holt.test.minitest.data.NotePolicy;

import java.util.Map;
import java.util.UUID;

@Activator(instantiateWithReflection = true)
public class NotesToNoteFormatterDNRequest implements NotesToNoteFormatterDNRequestRequirements {
    @Override
    public Map<UUID, Object> DN(NotePolicy input0) {
        return null;
    }
}
