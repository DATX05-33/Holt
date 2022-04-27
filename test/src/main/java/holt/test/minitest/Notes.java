package holt.test.minitest;


import holt.processor.annotation.Activator;
import holt.processor.annotation.Output;
import holt.processor.annotation.Traverse;
import holt.processor.generation.minitest.AbstractNotes;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NotePolicy;

import java.util.UUID;

@Traverse(
        name = "AN", // Add note
        startTypes = {
                @Output(type = Note.class),
                @Output(type = NotePolicy.class)
        },
        order = {"add_note_action", "insert_note"}
)
@Traverse(
        name = "DN", // Delete note
        startTypes = {
                @Output(type = UUID.class),
                @Output(type = NotePolicy.class)
        },
        order = {"remove_note_action", "delete_note"}
)
@Activator
public class Notes extends AbstractNotes {

        public void addNote(Note note) {
//                super.AN(note, new NotePolicy());
        }

        public void deleteNote(UUID uuid) {
//                super.DN(uuid, new NotePolicy());
        }

}
