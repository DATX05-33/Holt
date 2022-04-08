package holt.test.blast.privacy;

import holt.processor.annotation.QueriesFor;
import holt.test.blast.privacy.model.DataSubject;
import holt.test.blast.privacy.model.Policy;

@QueriesFor(EmailDBPolicy.class)
public class PolicyDBQuerier {

    public Policy getPolicyForDS(DataSubject dataSubject) {
        return new Policy(dataSubject.name() + " policy");
    }
}
