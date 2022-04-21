package holt.test.minitest;


import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesDatabaseRequirements;

import java.util.List;

@Activator(instantiateWithReflection = true)
public class NotesDatabase implements NotesDatabaseRequirements {

    public List<Note> getNotes() {
        return List.of(
                new Note("Wow wow wow wow"),
                new Note("Yes yes yes yes"),
                new Note("No no no no"),
                new Note("Glass")
        );
    }

}
