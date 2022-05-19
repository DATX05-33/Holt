package holt.test.minitest.notes_padfd;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesToNoteFormatterANLimitRequirements;
import holt.processor.generation.minitest.NotesToNoteFormatterDNLimitRequirements;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NotePolicy;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Activator(
        instantiateWithReflection = true,
        graphName = {
                "NotesToNoteFormatterANLimit",
                "NotesToNoteFormatterDNLimit"
        }
)
public class NotesToNoteFormatterLimit implements
        NotesToNoteFormatterANLimitRequirements,
        NotesToNoteFormatterDNLimitRequirements {

    @Override
    public Predicate<Note> AN(Map<Note, NotePolicy> input0) {
        return note -> true;
    }

    @Override
    public Predicate<UUID> DN(Map<UUID, NotePolicy> input0) {
        return uuid -> true;
    }
}
