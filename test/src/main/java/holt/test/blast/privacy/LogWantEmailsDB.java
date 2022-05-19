package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailBlasterLimitToSendLogToSendDatabaseToSendRequirements;
import holt.test.blast.privacy.model.LimitBlastEmails;

@Activator(graphName = "EmailBlasterLimitToSendLogToSendDatabaseToSend")
public class LogWantEmailsDB implements EmailBlasterLimitToSendLogToSendDatabaseToSendRequirements {
    @Override
    public void EB(LimitBlastEmails input) {

    }
}
