package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.emailBlast.EmailDBToEmailFetcherLimitfetchEmailsLimitFetchEmailsQuery;
import holt.processor.generation.emailBlast.EmailFetcherLimitfetchEmailsRequirements;
import holt.test.blast.EmailDB;
import holt.test.blast.model.Email;
import holt.test.blast.privacy.model.LimitFetchEmails;
import holt.test.blast.privacy.model.Policy;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blast.Main.emailBlast;

@FlowThrough(
        flow = emailBlast,
        functionName = "limitFetchEmails",
        outputType = LimitFetchEmails.class,
        queries = {
                @Query(
                        db = EmailDB.class,
                        type = Email.class
                )
        }
)
@Activator(graphName = "EmailFetcherLimitfetchEmails")
public class DBtoFetcherLimit implements EmailFetcherLimitfetchEmailsRequirements {

    @Override
    public EmailDBToEmailFetcherLimitfetchEmailsLimitFetchEmailsQuery queryEmailDBLimitFetchEmails(RequestPolicy input0) {
        return null;
    }

    @Override
    public LimitFetchEmails limitFetchEmails(Object dbInput0, RequestPolicy input1) {
        return null;
    }
}
