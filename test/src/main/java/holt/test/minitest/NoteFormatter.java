package holt.test.minitest;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.NoteFormatterRequirements;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NoteInsertion;

@FlowThrough(
        traverse = "AN",
        functionName = "AN",
        output = @Output(type = NoteInsertion.class)
)
@Activator(instantiateWithReflection = true)
public class NoteFormatter implements NoteFormatterRequirements {
    @Override
    public NoteInsertion AN(Note input0) {
        return null;
    }
}
