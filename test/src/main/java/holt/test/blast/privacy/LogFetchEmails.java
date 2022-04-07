package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailFetcherLimitfetchEmailsLogfetchEmailsRequirements;
import holt.test.blast.privacy.model.LimitFetchEmails;

import static holt.test.blast.Main.emailBlast;

@FlowThrough(
        flow = emailBlast,
        functionName = "logFetchEmails",
        outputType = LimitFetchEmails.class
)
@Activator(graphName = "EmailFetcherLimitfetchEmailsLogfetchEmails")
public class LogFetchEmails implements EmailFetcherLimitfetchEmailsLogfetchEmailsRequirements {

    @Override
    public LimitFetchEmails logFetchEmails(LimitFetchEmails input0) {
        return null;
    }
}
