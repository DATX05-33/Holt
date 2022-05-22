package holt.test.minitest;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesDatabaseCleanRequirements;

@Activator(instantiateWithReflection = true)
public class NotesDatabaseClean implements NotesDatabaseCleanRequirements {
}
