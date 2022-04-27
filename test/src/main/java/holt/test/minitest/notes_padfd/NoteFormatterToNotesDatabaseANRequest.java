package holt.test.minitest.notes_padfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.NoteFormatterToNotesDatabaseANRequestRequirements;
import holt.test.minitest.data.NoteInsertion;
import holt.test.minitest.data.NotePolicy;

import java.util.HashMap;
import java.util.Map;

@FlowThrough(
        traverse = "AN",
        functionName = "AN",
        output = @Output(type = NotePolicy.class)
)
@Activator(instantiateWithReflection = true)
public class NoteFormatterToNotesDatabaseANRequest implements NoteFormatterToNotesDatabaseANRequestRequirements {
    @Override
    public Map<NoteInsertion, NotePolicy> AN(NotePolicy input0) {
        return null;
    }
}
