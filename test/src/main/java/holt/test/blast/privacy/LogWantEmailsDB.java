package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailBlasterLimitblastEmailsLogblastEmailsDatabaseblastEmailsRequirements;
import holt.test.blast.privacy.model.LimitBlastEmails;

@Activator(graphName = "EmailBlasterLimitblastEmailsLogblastEmailsDatabaseblastEmails")
public class LogWantEmailsDB implements EmailBlasterLimitblastEmailsLogblastEmailsDatabaseblastEmailsRequirements {

    @Override
    public void emailBlast(LimitBlastEmails input) {

    }
}
