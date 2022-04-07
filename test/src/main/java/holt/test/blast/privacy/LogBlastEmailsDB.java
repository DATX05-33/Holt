package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailFetcherLimitwantBlastLogwantBlastDatabasewantBlastRequirements;
import holt.test.blast.privacy.model.LimitWantBlast;

@Activator(graphName = "EmailFetcherLimitwantBlastLogwantBlastDatabasewantBlast")
public class LogBlastEmailsDB implements EmailFetcherLimitwantBlastLogwantBlastDatabasewantBlastRequirements {
    @Override
    public void emailBlast(LimitWantBlast input) {

    }
}
