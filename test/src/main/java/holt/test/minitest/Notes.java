package holt.test.minitest;


import holt.processor.annotation.Activator;
import holt.processor.annotation.Output;
import holt.processor.annotation.Traverse;
import holt.processor.generation.minitest.AbstractNotes;
import holt.test.minitest.data.CountWordsAction;
import holt.test.minitest.data.CountWordsActionPolicy;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NotePolicy;

@Traverse(
        name = "AN",
        startTypes = {
                @Output(type = Note.class),
                @Output(type = NotePolicy.class)
        },
        order = {"add_note_action", "insert_note"}
)
@Activator
public class Notes extends AbstractNotes {
}
