package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailDBPolicyRequirements;

@Activator()
public class EmailDBPolicy implements EmailDBPolicyRequirements {
    @Override
    public PolicyDBQuerier getQuerierInstance() {
        return new PolicyDBQuerier();
    }
}
