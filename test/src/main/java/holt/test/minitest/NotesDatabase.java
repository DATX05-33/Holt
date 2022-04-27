package holt.test.minitest;


import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesDatabaseRequirements;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NoteInsertion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Activator(instantiateWithReflection = true)
public class NotesDatabase implements NotesDatabaseRequirements {

    private static final List<Note> notes = new ArrayList<>();

    public static final UUID n1 = UUID.randomUUID();
    public static final UUID n2 = UUID.randomUUID();

    static {
        notes.addAll(List.of(
                new Note("Wow wow wow wow", n1),
                new Note("Yes yes yes yes", n2)
        ));
    }

    public List<Note> getNotes() {
        return notes;
    }

    @Override
    public void DN(UUID input0) {
        for (Note note : notes) {
            if (note.id().equals(input0)) {
                notes.remove(note);
            }
        }
    }

    @Override
    public void AN(NoteInsertion newNote) {
        notes.add(newNote.note());
    }
}
