package holt.test.minitest;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.NoteFormatterRequirements;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NoteInsertion;

import java.util.UUID;

@FlowThrough(
        traverse = "AN",
        functionName = "AN",
        output = @Output(type = NoteInsertion.class)
)
@FlowThrough(
        traverse = "DN",
        functionName = "DN",
        output = @Output(type = UUID.class)
)
@Activator(instantiateWithReflection = true)
public class NoteFormatter implements NoteFormatterRequirements {

    @Override
    public UUID DN(UUID input0) {
        return input0;
    }

    @Override
    public NoteInsertion AN(Note input0) {
        return new NoteInsertion(input0);
    }
}
