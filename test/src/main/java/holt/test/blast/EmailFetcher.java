package holt.test.blast;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.QueryDefinition;
import holt.processor.generation.emailBlast.EmailDBToEmailFetcherFetchEmailsQuery;
import holt.processor.generation.emailBlast.EmailFetcherRequirements;
import holt.test.blast.model.Email;
import holt.test.blast.model.Emails;
import holt.test.blast.privacy.DBtoFetcherLimit;
import holt.test.blast.privacy.model.LimitBlastContent;
import holt.test.blast.privacy.model.LimitFetchEmails;

import static holt.test.blast.Main.EB;

@FlowThrough(
        traverse = EB,
        outputType = Emails.class,
        functionName = "fetchEmails",
        overrideQueries = {
                @QueryDefinition(
                        db = EmailDB.class,
                        process = DBtoFetcherLimit.class,
                        type = Email.class
                )
        }
)
@Activator
public class EmailFetcher implements EmailFetcherRequirements {
    @Override
    public Emails fetchEmails(LimitBlastContent input0, LimitFetchEmails input1) {
        return null;
    }

    @Override
    public EmailDBToEmailFetcherFetchEmailsQuery queryEmailDBFetchEmails(LimitBlastContent input0) {
        return null;
    }

}
