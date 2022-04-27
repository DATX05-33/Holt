package holt.test.minitest.notes_padfd.an;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesToNoteFormatterANLimitRequirements;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NotePolicy;

import java.util.Map;
import java.util.function.Predicate;

@Activator(instantiateWithReflection = true)
public class NotesToNoteFormatterANLimit implements NotesToNoteFormatterANLimitRequirements {

    @Override
    public Predicate<Note> AN(Map<Note, NotePolicy> input0) {
        return null;
    }
}
