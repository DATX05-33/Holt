package holt.test.minitest.notes_padfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.NotesToNoteFormatterANRequestRequirements;
import holt.processor.generation.minitest.NotesToNoteFormatterDNRequestRequirements;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NotePolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@FlowThrough(
        traverse = "AN",
        functionName = "AN",
        output = @Output(type = NotePolicy.class),
        forActivator = "NotesToNoteFormatterANRequest"
)
@FlowThrough(
        traverse = "DN",
        functionName = "DN",
        output = @Output(type = NotePolicy.class),
        forActivator = "NotesToNoteFormatterDNRequest"
)
@Activator(
        instantiateWithReflection = true
)
public class NotesToNoteFormatterRequest implements
        NotesToNoteFormatterDNRequestRequirements,
        NotesToNoteFormatterANRequestRequirements {
        @Override
        public Map<Note, NotePolicy> AN(NotePolicy input0) {
                return null;
        }

        @Override
        public Map<UUID, NotePolicy> DN(NotePolicy input0) {
                return null;
        }
}
