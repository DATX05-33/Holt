package holt.test.minitest.notes_padfd.dn;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesToNoteFormatterDNLimitRequirements;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Activator(instantiateWithReflection = true)
public class NotesToNoteFormatterDNLimit implements NotesToNoteFormatterDNLimitRequirements {
    @Override
    public Predicate<UUID> DN(Map<UUID, Object> input0) {
        return null;
    }
}
