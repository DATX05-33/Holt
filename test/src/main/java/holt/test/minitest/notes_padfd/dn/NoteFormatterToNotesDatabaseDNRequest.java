package holt.test.minitest.notes_padfd.dn;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.NoteFormatterToNotesDatabaseDNRequestRequirements;
import holt.test.minitest.data.NotePolicy;

import java.util.Map;

@FlowThrough(
        traverse = "DN",
        functionName = "DN",
        output = @Output(type = NotePolicy.class)
)
@Activator(instantiateWithReflection = true)
public class NoteFormatterToNotesDatabaseDNRequest implements NoteFormatterToNotesDatabaseDNRequestRequirements {

    @Override
    public Map<Object, NotePolicy> DN(Object input0) {
        return null;
    }
}
