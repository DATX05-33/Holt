package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailDBPolicyRequirements;

@Activator(graphName = "EmailDBPolicy")
public class PolicyDB implements EmailDBPolicyRequirements {
}
