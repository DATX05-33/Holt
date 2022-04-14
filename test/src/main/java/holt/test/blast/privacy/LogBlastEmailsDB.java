package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailFetcherLimitBlastContentLogBlastContentDatabaseBlastContentRequirements;
import holt.test.blast.privacy.model.LimitBlastContent;

@Activator(graphName = "EmailFetcherLimitBlastContentLogBlastContentDatabaseBlastContent")
public class LogBlastEmailsDB implements EmailFetcherLimitBlastContentLogBlastContentDatabaseBlastContentRequirements {
    @Override
    public void EB(LimitBlastContent input) {

    }
}
