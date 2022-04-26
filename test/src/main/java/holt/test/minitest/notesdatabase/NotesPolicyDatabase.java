package holt.test.minitest.notesdatabase;

import holt.processor.annotation.Activator;
import holt.processor.generation.minitest.NotesDatabasePolicyRequirements;

@Activator(instantiateWithReflection = true, graphName = "NotesDatabasePolicy")
public class NotesPolicyDatabase implements NotesDatabasePolicyRequirements {
}
