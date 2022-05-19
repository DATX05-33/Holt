package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailFetcherLimitEmailsLogEmailsRequirements;
import holt.test.blast.privacy.model.LimitFetchEmails;

import static holt.test.blast.Main.EB;

@FlowThrough(
        traverse = EB,
        functionName = "logFetchEmails",
        outputType = LimitFetchEmails.class
)
@Activator(graphName = "EmailFetcherLimitEmailsLogEmails")
public class LogFetchEmails implements EmailFetcherLimitEmailsLogEmailsRequirements {

    @Override
    public LimitFetchEmails logFetchEmails(LimitFetchEmails input0) {
        return null;
    }
}
