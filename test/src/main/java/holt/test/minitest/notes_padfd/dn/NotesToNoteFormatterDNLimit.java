package holt.test.minitest.notes_padfd.dn;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.NotesToNoteFormatterDNLimitRequirements;
import holt.test.minitest.data.NotePolicy;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Activator(instantiateWithReflection = true)
public class NotesToNoteFormatterDNLimit implements NotesToNoteFormatterDNLimitRequirements {
    @Override
    public Predicate<UUID> DN(Map<UUID, NotePolicy> input0) {
        return null;
    }
}
