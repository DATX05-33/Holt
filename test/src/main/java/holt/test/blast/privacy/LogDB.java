package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailBlasterLimitblastEmailsLogblastEmailsDatabaseblastEmailsRequirements;
import holt.processor.generation.emailBlast.EmailFetcherLimitfetchEmailsLogfetchEmailsDatabasefetchEmailsRequirements;
import holt.processor.generation.emailBlast.EmailFetcherLimitwantBlastLogwantBlastDatabasewantBlastRequirements;

@Activator
public class LogDB implements
        EmailFetcherLimitwantBlastLogwantBlastDatabasewantBlastRequirements,
        EmailFetcherLimitfetchEmailsLogfetchEmailsDatabasefetchEmailsRequirements,
        EmailBlasterLimitblastEmailsLogblastEmailsDatabaseblastEmailsRequirements {
    @Override
    public void emailBlast(Object input) {

    }
}
