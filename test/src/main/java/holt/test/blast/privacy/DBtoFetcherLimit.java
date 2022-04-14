package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.emailBlast.EmailFetcherLimitEmailsRequirements;
import holt.test.blast.EmailDB;
import holt.test.blast.model.Email;
import holt.test.blast.privacy.model.LimitFetchEmails;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blast.Main.EB;

@FlowThrough(
        traverse = EB,
        functionName = "limitFetchEmails",
        outputType = LimitFetchEmails.class,
        queries = {
//                @Query(
//                        db = EmailDB.class,
//                        type = Email.class
//                )
        }
)
@Activator(graphName = "EmailFetcherLimitEmails")
public class DBtoFetcherLimit implements EmailFetcherLimitEmailsRequirements {
    @Override
    public LimitFetchEmails limitFetchEmails(Object dbInput0, RequestPolicy input1) {
        return null;
    }
}
