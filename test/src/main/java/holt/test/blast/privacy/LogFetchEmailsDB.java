package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailFetcherLimitEmailsLogEmailsDatabaseEmailsRequirements;
import holt.test.blast.privacy.model.LimitFetchEmails;

@Activator(graphName = "EmailFetcherLimitEmailsLogEmailsDatabaseEmails")
public class LogFetchEmailsDB implements EmailFetcherLimitEmailsLogEmailsDatabaseEmailsRequirements {

    @Override
    public void EB(LimitFetchEmails input) {

    }
}
