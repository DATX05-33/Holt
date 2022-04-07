package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailFetcherLimitfetchEmailsLogfetchEmailsDatabasefetchEmailsRequirements;
import holt.test.blast.privacy.model.LimitFetchEmails;

@Activator(graphName = "EmailFetcherLimitfetchEmailsLogfetchEmailsDatabasefetchEmails")
public class LogFetchEmailsDB implements EmailFetcherLimitfetchEmailsLogfetchEmailsDatabasefetchEmailsRequirements {

    @Override
    public void emailBlast(LimitFetchEmails input) {

    }
}
