package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailDBToEmailFetcherLimitfetchEmailsLimitFetchEmailsQuery;
import holt.processor.generation.emailBlast.EmailFetcherLimitfetchEmailsRequirements;
import holt.test.blast.privacy.model.LimitFetchEmails;
import holt.test.blast.privacy.model.Policy;

import static holt.test.blast.Main.emailBlast;

@FlowThrough(
        flow = emailBlast,
        functionName = "limitFetchEmails",
        outputType = LimitFetchEmails.class
)
@Activator(graphName = "EmailFetcherLimitfetchEmails")
public class DBtoFetcherLimit implements EmailFetcherLimitfetchEmailsRequirements {

    @Override
    public EmailDBToEmailFetcherLimitfetchEmailsLimitFetchEmailsQuery queryEmailDBLimitFetchEmails(Policy input0) {
        return null;
    }

    @Override
    public LimitFetchEmails limitFetchEmails(Object dbInput0, Policy input1) {
        return null;
    }
}
