package holt.test.blast;

import holt.processor.annotation.QueriesFor;
import holt.test.blast.model.Email;
import holt.test.blast.privacy.model.DataSubject;

@QueriesFor(EmailDB.class)
public class EmailDBQuerier {

    public Email getEmailByDS(DataSubject dataSubject) {
        return new Email(dataSubject.name() + "@email.com");
    }

}
