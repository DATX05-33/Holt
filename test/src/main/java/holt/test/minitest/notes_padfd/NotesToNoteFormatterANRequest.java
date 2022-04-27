package holt.test.minitest.notes_padfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.NotesToNoteFormatterANRequestRequirements;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NotePolicy;

import java.util.Map;

@FlowThrough(
        traverse = "AN",
        functionName = "AN",
        output = @Output(type = NotePolicy.class)
)
@Activator(instantiateWithReflection = true)
public class NotesToNoteFormatterANRequest implements NotesToNoteFormatterANRequestRequirements {
    @Override
    public Map<Note, NotePolicy> AN(NotePolicy input0) {
        return null;
    }
}
