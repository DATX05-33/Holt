package holt.test.minitest.notes_padfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.NoteFormatterReasonRequirements;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NotePolicy;

import java.util.Map;
import java.util.UUID;

@FlowThrough(
        traverse = "AN",
        functionName = "AN",
        output = @Output(type = NotePolicy.class)
)
@Activator(instantiateWithReflection = true)
public class NoteFormatterReason implements NoteFormatterReasonRequirements {

    @Override
    public Object DN(Map<UUID, NotePolicy> input0) {
        return null;
    }

    @Override
    public NotePolicy AN(Map<Note, NotePolicy> input0) {
        return null;

    }
}
